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
 * Desc: 交易域支付成功聚合统计
 * 需要启动的进程
 * zk、kafka、maxwell、clickhouse、DwdTradeOrderPreProcess、DwdTradeOrderDetail、
 * DwdTradePayDetailSuc、DwsTradePaymentSucWindow
 */
public class DwsTradePaymentSucWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 设置流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);

        //TODO 2.检查点相关设置(略)

        //TODO 3.从kafka的dwd_trade_pay_detail_suc主题中读取加购明细数据
        //3.1 声明消费主题以及消费者组
        String topic = "dwd_trade_pay_detail_suc";
        String groupId = "dws_trade_payment_suc_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据进行类型转换   jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //{"callback_time":"2022-06-01 11:15:34","payment_type_name":"微信","sku_num":"2",
        // "split_payment_amount":"8976.0","split_original_amount":"8976.0000","payment_type_code":"1102",
        // "sku_id":"16","source_type_name":"商品推广","order_detail_id":"590","user_id":"14",
        // "province_id":"20","source_type_code":"2402","row_op_ts":"2022-06-01 03:15:12.373Z",
        // "sku_name":"华为 HUAWEI P40 麒麟990 5G SoC芯片 5000万超感知徕卡三摄 30倍数字变焦 8GB+128GB亮黑色全网通5G手机",
        // "source_id":"31","order_id":"251","ts":"1654053314"}
        //jsonObjDS.print(">>>");

        //TODO 5.指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = jsonObjDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<JSONObject>() {
                        @Override
                        public long extractTimestamp(JSONObject josnObj, long recordTimestamp) {
                            return josnObj.getLong("ts") * 1000;
                        }
                    }
                )
        );

        //TODO 6.按照用户id进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjWithWatermarkDS.keyBy(jsonObj -> jsonObj.getString("user_id"));

        //TODO 7.使用flink状态编程  过滤出支付成功独立用户以及首次支付成功用户
        SingleOutputStreamOperator<TradePaymentBean> processDS = keyedDS.process(
            new KeyedProcessFunction<String, JSONObject, TradePaymentBean>() {
                private ValueState<String> lastPaymentDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastPaymentDateState
                        = getRuntimeContext().getState(new ValueStateDescriptor<String>("lastPaymentDateState", String.class));
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<TradePaymentBean> out) throws Exception {
                    //从状态中获取上次支付成功日期
                    String lastPaymentDate = lastPaymentDateState.value();
                    //获取当前支付成功日期
                    long ts = jsonObj.getLong("ts") * 1000;
                    String curPaymentDate = DateFormatUtil.toDate(ts);

                    Long uuCt = 0L;
                    Long newUserCt = 0L;
                    if (StringUtils.isEmpty(lastPaymentDate)) {
                        //如果状态中的日期为空，说明当前用户以前没有做过支付操作  这次是第一次
                        uuCt = 1L;
                        newUserCt = 1L;
                        lastPaymentDateState.update(curPaymentDate);
                    } else {
                        //如果状态中的日期不为空，说明当前用户以前支付成功过，肯定不是首次支付成功用户
                        if (!lastPaymentDate.equals(curPaymentDate)) {
                            uuCt = 1L;
                            lastPaymentDateState.update(curPaymentDate);
                        }
                    }

                    if (uuCt != 0L || newUserCt != 0L) {
                        out.collect(
                            new TradePaymentBean("", "", uuCt, newUserCt, 0L)
                        );
                    }

                }
            }
        );

        //TODO 8.开窗
        AllWindowedStream<TradePaymentBean, TimeWindow> windowDS = processDS.windowAll(TumblingEventTimeWindows.of(Time.seconds(10)));

        //TODO 9.聚合计算
        SingleOutputStreamOperator<TradePaymentBean> reduceDS = windowDS.reduce(
            new ReduceFunction<TradePaymentBean>() {
                @Override
                public TradePaymentBean reduce(TradePaymentBean value1, TradePaymentBean value2) throws Exception {
                    value1.setPaymentSucUniqueUserCount(value1.getPaymentSucUniqueUserCount() + value2.getPaymentSucUniqueUserCount());
                    value1.setPaymentSucNewUserCount(value1.getPaymentSucNewUserCount() + value2.getPaymentSucNewUserCount());
                    return value1;
                }
            },
            new AllWindowFunction<TradePaymentBean, TradePaymentBean, TimeWindow>() {
                @Override
                public void apply(TimeWindow window, Iterable<TradePaymentBean> values, Collector<TradePaymentBean> out) throws Exception {
                    for (TradePaymentBean paymentBean : values) {
                        paymentBean.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                        paymentBean.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));
                        paymentBean.setTs(System.currentTimeMillis());
                        out.collect(paymentBean);
                    }
                }
            }
        );

        //TODO 10.将计算的结果写到ck中
        reduceDS.print(">>");
        reduceDS.addSink(
            MyClickHouseUtil.getJdbcSink("insert into dws_trade_payment_suc_window values(?,?,?,?,?)")
        );
        env.execute();
    }
}
