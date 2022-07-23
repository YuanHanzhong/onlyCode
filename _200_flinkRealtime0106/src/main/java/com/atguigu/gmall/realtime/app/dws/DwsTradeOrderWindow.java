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
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

/**
 * Author: Felix
 * Date: 2022/7/13
 * Desc: 交易域-下单相关聚合统计
 * 需要启动的进程
 *      zk、kafka、maxwell、ClickHouse、DwdTradeOrderPreProcess
 *      DwdTradeOrderDetail、DwsTradeOrderWindow
 */
public class DwsTradeOrderWindow {
    public static void main(String[] args) throws Exception {
        // TODO 1. 环境准备
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        // TODO 2. 检查点相关设置(略)

        // TODO 3. 从 Kafka dwd_trade_order_detail 读取订单明细数据
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_order_window";
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        DataStreamSource<String> source = env.addSource(kafkaConsumer);

        // TODO 4. 转换数据结构
        SingleOutputStreamOperator<JSONObject> mappedStream = source.map(JSON::parseObject);
        //{"create_time":"2022-07-08 16:22:11","sku_num":"1","split_original_amount":"4488.0000",
        // "sku_id":"16","date_id":"2022-07-08","source_type_name":"用户查询","user_id":"55",
        // "province_id":"29","source_type_code":"2401","row_op_ts":"2022-07-13 08:22:12.589Z",
        // "sku_name":"华为 HUAWEI P40 麒麟990 5G S色全网通5G手机","id":"315","order_id":"152",
        // "split_total_amount":"4488.0","ts":"1657700531"}
        //mappedStream.print(">>>>");
        // TODO 5. 设置水位线
        SingleOutputStreamOperator<JSONObject> withWatermarkStream = mappedStream.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<JSONObject>forMonotonousTimestamps()
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<JSONObject>() {
                        @Override
                        public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                            return jsonObj.getLong("ts") * 1000;
                        }
                    }
                )
        );

        // TODO 6. 按照用户 id 分组
        KeyedStream<JSONObject, String> keyedByUserIdStream = withWatermarkStream.keyBy(r -> r.getString("user_id"));

        // TODO 7. 统计当日下单独立用户数和新增下单用户数(首单)
        SingleOutputStreamOperator<TradeOrderBean> processDS = keyedByUserIdStream.process(
            new KeyedProcessFunction<String, JSONObject, TradeOrderBean>() {
                //定义一个状态，用于存放上次下单日期
                private ValueState<String> lastOrderDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastOrderDateState
                        = getRuntimeContext().getState(new ValueStateDescriptor<String>("lastOrderDateState", String.class));
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<TradeOrderBean> out) throws Exception {
                    String lastOrderDate = lastOrderDateState.value();
                    String curOrderDate = DateFormatUtil.toDate(jsonObj.getLong("ts") * 1000);
                    Long firstCt = 0L;
                    Long uuCt = 0L;
                    if (StringUtils.isEmpty(lastOrderDate)) {
                        //如果状态中上次下单日期为空，说明以前从没有下单过，首单计数，当前独立订单计数
                        firstCt = 1L;
                        uuCt = 1L;
                        lastOrderDateState.update(curOrderDate);
                    } else {
                        //如果状态中上次下单日期不为空，说明以前下单过，判断上次下单日期是否为今天
                        if (!lastOrderDate.equals(curOrderDate)) {
                            uuCt = 1L;
                            lastOrderDateState.update(curOrderDate);
                        }
                    }
                    if (uuCt != 0L || firstCt != 0L) {
                        out.collect(new TradeOrderBean(
                            "", "", uuCt, firstCt, 0L
                        ));
                    }
                }
            }
        );

        // TODO 8. 开窗
        AllWindowedStream<TradeOrderBean, TimeWindow> windowDS = processDS.windowAll(TumblingEventTimeWindows.of(
            org.apache.flink.streaming.api.windowing.time.Time.seconds(10L)));

        // TODO 9. 聚合
        SingleOutputStreamOperator<TradeOrderBean> reducedStream = windowDS.reduce(
            new ReduceFunction<TradeOrderBean>() {
                @Override
                public TradeOrderBean reduce(TradeOrderBean value1, TradeOrderBean value2) throws Exception {
                    value1.setOrderNewUserCount(value1.getOrderNewUserCount() + value2.getOrderNewUserCount());
                    value1.setOrderUniqueUserCount(value1.getOrderUniqueUserCount() + value2.getOrderUniqueUserCount());
                    return value1;
                }
            },
            new ProcessAllWindowFunction<TradeOrderBean, TradeOrderBean, TimeWindow>() {
                @Override
                public void process(Context context, Iterable<TradeOrderBean> values, Collector<TradeOrderBean> out) throws Exception {
                    String stt = DateFormatUtil.toYmdHms(context.window().getStart());
                    String edt = DateFormatUtil.toYmdHms(context.window().getEnd());

                    for (TradeOrderBean value : values) {
                        value.setStt(stt);
                        value.setEdt(edt);
                        value.setTs(System.currentTimeMillis());
                        out.collect(value);
                    }
                }
            }
        );

        // TODO 10. 写出到 OLAP 数据库
        reducedStream.print(">>");
        SinkFunction<TradeOrderBean> jdbcSink = MyClickHouseUtil.getJdbcSinkFunction(
            "insert into dws_trade_order_window values(?,?,?,?,?)"
        );
        reducedStream.addSink(jdbcSink);

        env.execute();
    }
}
