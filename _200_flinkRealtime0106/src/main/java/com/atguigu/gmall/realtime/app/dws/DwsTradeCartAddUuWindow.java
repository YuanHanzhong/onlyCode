package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.CartAddUuBean;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * Author: Felix
 * Date: 2022/7/13
 * Desc: 交易域-加购聚合统计
 * 需要启动的进程
 *      zk、kafka、maxwell、clickhouse、DwdTradeCartAdd、DwsTradeCartAddUuWindow
 */
public class DwsTradeCartAddUuWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //TODO 2.检查点相关设置(略)
        //TODO 3.从kafka主题中(加购事实表)读取加购数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_trade_cart_add";
        String groupId = "dws_trade_cart_add_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据类型进行转换       jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);
        //{"sku_num":"3","uesr_id":"173","sku_id":"35","source_type":"2404","id":"1933","source_type_name":"促销活动","ts":"1657693861"}
        //jsonObjDS.print(">>>");

        //TODO 5.指定watermark以及提取事件时间字段
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = jsonObjDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<JSONObject>() {
                        @Override
                        public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                            return jsonObj.getLong("ts") * 1000L;
                        }
                    }
                )
        );

        //TODO 6.按照uid进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjWithWatermarkDS.keyBy(
            jsonObj -> jsonObj.getString("uesr_id")
        );

        //TODO 7.使用Flink的状态编程 判读是否为加购独立用户
        SingleOutputStreamOperator<JSONObject> processDS = keyedDS.process(
            new KeyedProcessFunction<String, JSONObject, JSONObject>() {
                private ValueState<String> lastCartDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    ValueStateDescriptor<String> valueStateDescriptor = new ValueStateDescriptor<String>("lastCartDateState", String.class);
                    valueStateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(1)).build());
                    lastCartDateState = getRuntimeContext().getState(valueStateDescriptor);
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<JSONObject> out) throws Exception {
                    String lastCartDate = lastCartDateState.value();
                    String curCartDate = DateFormatUtil.toDate(jsonObj.getLong("ts") * 1000L);
                    if (StringUtils.isEmpty(lastCartDate) || !curCartDate.equals(lastCartDate)) {
                        out.collect(jsonObj);
                    }
                }
            }
        );

        //TODO 8.开窗
        AllWindowedStream<JSONObject, TimeWindow> windowDS
            = processDS.windowAll(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10)));

        //TODO 9.聚合计算
        SingleOutputStreamOperator<CartAddUuBean> aggregateDS = windowDS.aggregate(
            new AggregateFunction<JSONObject, Long, Long>() {
                @Override
                public Long createAccumulator() {
                    return 0L;
                }

                @Override
                public Long add(JSONObject value, Long accumulator) {
                    return ++accumulator;
                }

                @Override
                public Long getResult(Long accumulator) {
                    return accumulator;
                }

                @Override
                public Long merge(Long a, Long b) {
                    return null;
                }
            },
            new AllWindowFunction<Long, CartAddUuBean, TimeWindow>() {
                @Override
                public void apply(TimeWindow window, Iterable<Long> values, Collector<CartAddUuBean> out) throws Exception {
                    for (Long value : values) {
                        out.collect(new CartAddUuBean(
                            DateFormatUtil.toYmdHms(window.getStart()),
                            DateFormatUtil.toYmdHms(window.getEnd()),
                            value,
                            System.currentTimeMillis()
                        ));
                    }
                }
            }
        );

        //TODO 10.将计算的结果写到ClickHouse中]
        aggregateDS.print(">>>");
        aggregateDS.addSink(
            MyClickHouseUtil.getJdbcSinkFunction("insert into dws_trade_cart_add_uu_window values(?,?,?,?)")
        );
        env.execute();
    }
}
