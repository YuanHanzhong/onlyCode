package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.TradePaymentBean;
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
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

/**
 * Author: Felix
 * Date: 2022/7/13
 * Desc: 交易域-支付成功指标聚合统计
 * 需要启动的进程
 *      zk、kafka、maxwell、clickhouse、DwdTradeOrderPreProcess
 *      DwdTradeOrderDetail、DwdTradePayDetailSuc、DwsTradePaymentSucWindow
 *
 */
public class DwsTradePaymentSucWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //TODO 2.检查点相关设置(略)
        //TODO 3.从kafka主题中(加购事实表)读取加购数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_trade_pay_detail_suc";
        String groupId = "dws_trade_payment_suc_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据类型进行转换       jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);
        //{"callback_time":"2022-07-08 15:25:38","payment_type_name":"微信","sku_num":"1",
        // "split_payment_amount":"40.0","split_original_amount":"40.0000",
        // "payment_type_code":"1102","sku_id":"23","source_type_name":"商品推广",
        // "order_detail_id":"273","user_id":"157","province_id":"8",
        // "source_type_code":"2402","row_op_ts":"2022-07-13 07:25:19.026Z",
        // "sku_name":"十月稻田 辽河长粒香 东北大米 5kg","source_id":"70","order_id":"128","ts":"1657697118"}
        //jsonObjDS.print(">>>>");
        // TODO 5. 设置水位线
        SingleOutputStreamOperator<JSONObject> withWatermarkSecondStream = jsonObjDS.assignTimestampsAndWatermarks(
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
        KeyedStream<JSONObject, String> keyedByUserIdStream = withWatermarkSecondStream.keyBy(r -> r.getString("user_id"));

        //TODO 7.使用状态编程 过滤出支付成功独立用户以及首次支付用户
        SingleOutputStreamOperator<TradePaymentBean> processDS = keyedByUserIdStream.process(
            new KeyedProcessFunction<String, JSONObject, TradePaymentBean>() {
                //定义一个状态，记录上次支付成功日期
                private ValueState<String> lastPaymentSucDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastPaymentSucDateState
                        = getRuntimeContext().getState(new ValueStateDescriptor<String>("lastPaymentSucDateState", String.class));
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<TradePaymentBean> out) throws Exception {
                    String lastPaymentSucDate = lastPaymentSucDateState.value();
                    String curPaymentSucDate = DateFormatUtil.toDate(jsonObj.getLong("ts") * 1000);
                    Long firstCt = 0L;
                    Long uuCt = 0L;
                    if (StringUtils.isEmpty(lastPaymentSucDate)) {
                        //若末次支付日期为 null，则将首次支付用户数和支付独立用户数均置为 1；
                        firstCt = 1L;
                        uuCt = 1L;
                        lastPaymentSucDateState.update(curPaymentSucDate);
                    } else {
                        //状态中上次支付日期不为空，说明已经支付成功过了，首次支付还是0；
                        //判断末次支付日期是否为当日，如果不是当日则支付独立用户数置为 1，否则置为 0。最后将状态中的支付日期更新为当日。
                        if (!lastPaymentSucDate.equals(curPaymentSucDate)) {
                            uuCt = 1L;
                            lastPaymentSucDateState.update(curPaymentSucDate);
                        }
                    }
                    if (uuCt != 0L || firstCt != 0L) {
                        out.collect(new TradePaymentBean(
                            "", "", uuCt, firstCt, 0L
                        ));
                    }
                }
            }
        );

        //TODO 8. 开窗
        AllWindowedStream<TradePaymentBean, TimeWindow> windowDS = processDS.windowAll(TumblingEventTimeWindows.of(
            org.apache.flink.streaming.api.windowing.time.Time.seconds(10L)));

        //TODO 9. 聚合计算
        SingleOutputStreamOperator<TradePaymentBean> reduceDS = windowDS.reduce(
            new ReduceFunction<TradePaymentBean>() {
                @Override
                public TradePaymentBean reduce(TradePaymentBean value1, TradePaymentBean value2) throws Exception {
                    value1.setPaymentSucNewUserCount(value1.getPaymentSucNewUserCount() + value2.getPaymentSucNewUserCount());
                    value1.setPaymentSucUniqueUserCount(value1.getPaymentSucUniqueUserCount() + value2.getPaymentSucUniqueUserCount());
                    return value1;
                }
            },
            new ProcessAllWindowFunction<TradePaymentBean, TradePaymentBean, TimeWindow>() {
                @Override
                public void process(Context context, Iterable<TradePaymentBean> elements, Collector<TradePaymentBean> out) throws Exception {
                    for (TradePaymentBean paymentBean : elements) {
                        paymentBean.setStt(DateFormatUtil.toYmdHms(context.window().getStart()));
                        paymentBean.setEdt(DateFormatUtil.toYmdHms(context.window().getEnd()));
                        paymentBean.setTs(System.currentTimeMillis());
                        out.collect(paymentBean);
                    }
                }
            }
        );
        //TODO 10.将聚合的结果写到ClickHouse中
        reduceDS.print(">>>");
        reduceDS.addSink(
            MyClickHouseUtil.getJdbcSinkFunction("insert into dws_trade_payment_suc_window values(?,?,?,?,?)")
        );

        env.execute();
    }
}
