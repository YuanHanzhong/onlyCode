package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.TradeOrderBean;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * Author: Felix
 * Date: 2022/6/1
 * Desc: 交易域下单聚合统计
 * 需要启动的进程
 *      zk、kafka、maxwell、clickhouse、DwdTradeOrderPreProcess、DwdTradeOrderDetail、DwsTradeOrderWindow
 *
 */
public class DwsTradeOrderWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //TODO 2.检查点相关设置(略)
        //TODO 3.从kafka的dwd_trade_order_detail主题中读取数据
        //3.1 声明消费的主题 以及消费者组
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_order_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
        //TODO 4.对读取的数据进行类型转换  jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //{"create_time":"2022-06-01 14:27:16","sku_num":"2","activity_rule_id":"2","split_original_amount":"18394.0000",
        // "sku_id":"12","date_id":"2022-06-01","source_type_name":"商品推广",
        // "user_id":"309","province_id":"17","source_type_code":"2402","row_op_ts":"2022-06-01 06:27:18.025Z",
        // "activity_id":"1","sku_name":"Apple iPhone 12 (A2404) 128GB 黑色 支持移动联通电信5G 双卡双待手机","id":"707",
        // "source_id":"24","order_id":"297","split_activity_amount":"1200.0","split_total_amount":"17194.0","ts":"1654064836"}
        //jsonObjDS.print(">>>");

        //TODO 5.指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = jsonObjDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<JSONObject>() {
                        @Override
                        public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                            return jsonObj.getLong("ts")*1000;
                        }
                    }
                )
        );

        //TODO 6.按照用户id进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjWithWatermarkDS.keyBy(jsonObj -> jsonObj.getString("user_id"));

        //TODO 7.使用flink的状态编程  对独立下单用户以及首次下单用户进行计数
        SingleOutputStreamOperator<TradeOrderBean> processDS = keyedDS.process(
            new KeyedProcessFunction<String, JSONObject, TradeOrderBean>() {
                private ValueState<String> lastOrderDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastOrderDateState
                        = getRuntimeContext().getState(new ValueStateDescriptor<String>("lastOrderDateState", String.class));
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<TradeOrderBean> out) throws Exception {
                    //获取上次下单时间
                    String lastOrderDate = lastOrderDateState.value();
                    //获取本次下单时间
                    long ts = jsonObj.getLong("ts") * 1000;
                    String curOrderDate = DateFormatUtil.toDate(ts);
                    Long uuCt = 0L;
                    Long newOrderCt = 0L;
                    if (StringUtils.isEmpty(lastOrderDate)) {
                        uuCt = 1L;
                        newOrderCt = 1L;
                        lastOrderDateState.update(curOrderDate);
                    } else {
                        if (!lastOrderDate.equals(curOrderDate)) {
                            uuCt = 1L;
                            lastOrderDateState.update(curOrderDate);
                        }
                    }
                    if (uuCt != 0L || newOrderCt != 0L) {
                        out.collect(
                            new TradeOrderBean("", "", uuCt, newOrderCt, 0L)
                        );
                    }
                }
            }
        );

        //TODO 8.开窗
        AllWindowedStream<TradeOrderBean, TimeWindow> windowDS = processDS.windowAll(TumblingEventTimeWindows.of(Time.seconds(10)));

        //TODO 9.聚合计算
        SingleOutputStreamOperator<TradeOrderBean> reduceDS = windowDS.reduce(
            new ReduceFunction<TradeOrderBean>() {
                @Override
                public TradeOrderBean reduce(TradeOrderBean value1, TradeOrderBean value2) throws Exception {
                    value1.setOrderUniqueUserCount(value1.getOrderUniqueUserCount() + value2.getOrderUniqueUserCount());
                    value1.setOrderNewUserCount(value1.getOrderNewUserCount() + value2.getOrderNewUserCount());
                    return value1;
                }
            },
            new AllWindowFunction<TradeOrderBean, TradeOrderBean, TimeWindow>() {
                @Override
                public void apply(TimeWindow window, Iterable<TradeOrderBean> values, Collector<TradeOrderBean> out) throws Exception {
                    for (TradeOrderBean orderBean : values) {
                        orderBean.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                        orderBean.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));
                        orderBean.setTs(System.currentTimeMillis());
                        out.collect(orderBean);
                    }
                }
            }
        );

        //TODO 10.将计算结果写到CK中
        reduceDS.print(">>>>");
        reduceDS.addSink(
            MyClickHouseUtil.getJdbcSink("insert into dws_trade_order_window values(?,?,?,?,?)")
        );

        env.execute();
    }
}
