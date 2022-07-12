package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.app.func.DimAsyncFunction;
import com.atguigu.gmall.realtime.bean.TradeProvinceOrderBean;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import com.atguigu.gmall.realtime.util.TimestampLtz3CompareUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Author: Felix
 * Date: 2022/6/6
 * Desc: 交易域按照省份分组对应订单数以及订单金额进行聚合
 * 需要启动的进程
 *      zk、kafka、maxwell、hdfs、hbase、redis、clickhouse、
 *      DwsTradeProvinceOrderWindow、DwdTradeOrderDetail、DwdTradeOrderPreProcess
 *
 */
public class DwsTradeProvinceOrderWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        /*
        //TODO 2.检查点相关的设置
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30),Time.seconds(3)));
        env.setStateBackend(new HashMapStateBackend());
        //env.getCheckpointConfig().setCheckpointStorage(new JobManagerCheckpointStorage());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/xxx");
        System.setProperty("HADOOP_USER_NAME","atguigu");
        */
        //TODO 3.从kakfa主题中读取数据
        //3.1 声明消费主题以及消费者组
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_province_order_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据进行类型转换   jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //{"create_time":"2022-06-06 15:32:14","sku_num":"1","split_original_amount":"40.0000","sku_id":"23","date_id":"2022-06-06",
        // "source_type_name":"用户查询","user_id":"866","province_id":"19","source_type_code":"2401","row_op_ts":"2022-06-06 07:32:15.914Z",
        // "sku_name":"十月稻田 辽河长粒香 东北大米 5kg","id":"4212","order_id":"1746","split_total_amount":"40.0","ts":"1654500735"}
        //jsonObjDS.print(">>>>");

        //TODO 5.按照唯一键order_detail_id进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjDS.keyBy(jsonObj -> jsonObj.getString("id"));

        //TODO 6.使用Flink的状态编程对重复数据进行去重
        SingleOutputStreamOperator<JSONObject> processDS = keyedDS.process(
            new KeyedProcessFunction<String, JSONObject, JSONObject>() {
                private ValueState<JSONObject> lastValueState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastValueState
                        = getRuntimeContext().getState(new ValueStateDescriptor<JSONObject>("lastValueState",JSONObject.class));
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<JSONObject> out) throws Exception {
                    //从状态中获取数据
                    JSONObject lastValue = lastValueState.value();
                    if(lastValue == null){
                        //如果状态中还没有数据，说明当前这条数据  是第一条订单明细数据，将其放到状态中
                        long currentProcessingTime = ctx.timerService().currentProcessingTime();
                        ctx.timerService().registerProcessingTimeTimer(currentProcessingTime + 5000L);
                        lastValueState.update(jsonObj);
                    }else{
                        String lastRowOpTs = lastValue.getString("row_op_ts");
                        String curRowOpTs = jsonObj.getString("row_op_ts");
                        if(TimestampLtz3CompareUtil.compare(lastRowOpTs,curRowOpTs)<=0){
                            lastValueState.update(jsonObj);
                        }
                    }
                }

                @Override
                public void onTimer(long timestamp, OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
                    JSONObject lastValue = lastValueState.value();
                    if(lastValue != null){
                        out.collect(lastValue);
                    }
                    lastValueState.clear();
                }
            }
        );

        //TODO 7.对去重后的数据进行类型转换  jsonObj->TradeProvinceOrderBean实体类对象
        SingleOutputStreamOperator<TradeProvinceOrderBean> orderBeanDS = processDS.map(
            new MapFunction<JSONObject, TradeProvinceOrderBean>() {
                @Override
                public TradeProvinceOrderBean map(JSONObject jsonObj) throws Exception {
                    return TradeProvinceOrderBean.builder()
                        .provinceId(jsonObj.getString("province_id"))
                        .orderIdSet(new HashSet<String>(Collections.singleton(jsonObj.getString("order_id"))))
                        .orderAmount(jsonObj.getDouble("split_total_amount"))
                        .ts(jsonObj.getLong("ts") * 1000)
                        .build();
                }
            }
        );

        //TODO 8.指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<TradeProvinceOrderBean> withWatermarkDS = orderBeanDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<TradeProvinceOrderBean>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<TradeProvinceOrderBean>() {
                        @Override
                        public long extractTimestamp(TradeProvinceOrderBean tradeProvinceOrderBean, long recordTimestamp) {
                            return tradeProvinceOrderBean.getTs();
                        }
                    }
                )
        );

        //TODO 9.按照省份id进行分组
        KeyedStream<TradeProvinceOrderBean, String> provinceKeyedDS
            = withWatermarkDS.keyBy(TradeProvinceOrderBean::getProvinceId);

        //TODO 10.开窗
        WindowedStream<TradeProvinceOrderBean, String, TimeWindow> windowDS = provinceKeyedDS.window(TumblingEventTimeWindows.of(Time.seconds(10)));

        //TODO 11.聚合计算
        SingleOutputStreamOperator<TradeProvinceOrderBean> reduceDS = windowDS.reduce(
            new ReduceFunction<TradeProvinceOrderBean>() {
                @Override
                public TradeProvinceOrderBean reduce(TradeProvinceOrderBean value1, TradeProvinceOrderBean value2) throws Exception {
                    value1.getOrderIdSet().addAll(value2.getOrderIdSet());
                    value1.setOrderCount((long) value1.getOrderIdSet().size());
                    value1.setOrderAmount(value1.getOrderAmount() + value2.getOrderAmount());
                    return value1;
                }
            },
            new WindowFunction<TradeProvinceOrderBean, TradeProvinceOrderBean, String, TimeWindow>() {
                @Override
                public void apply(String s, TimeWindow window, Iterable<TradeProvinceOrderBean> input, Collector<TradeProvinceOrderBean> out) throws Exception {
                    for (TradeProvinceOrderBean tradeProvinceOrderBean : input) {
                        tradeProvinceOrderBean.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                        tradeProvinceOrderBean.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));
                        tradeProvinceOrderBean.setTs(System.currentTimeMillis());
                        out.collect(tradeProvinceOrderBean);
                    }
                }
            }
        );

        //TODO 12.将省份名称关联进来
        SingleOutputStreamOperator<TradeProvinceOrderBean> withProvinceDS = AsyncDataStream.unorderedWait(
            reduceDS,
            new DimAsyncFunction<TradeProvinceOrderBean>("dim_base_province") {
                @Override
                public void join(TradeProvinceOrderBean tradeProvinceOrderBean, JSONObject dimInfoJsonObj) {
                    tradeProvinceOrderBean.setProvinceName(dimInfoJsonObj.getString("NAME"));
                }

                @Override
                public String getKey(TradeProvinceOrderBean tradeProvinceOrderBean) {
                    return tradeProvinceOrderBean.getProvinceId();
                }
            },
            60, TimeUnit.SECONDS
        );


        //TODO 13.将聚合结果写到ClickHouse数据库中
        withProvinceDS.print(">>>>");
        withProvinceDS.addSink(
            MyClickHouseUtil.getJdbcSink("insert into dws_trade_province_order_window values(?,?,?,?,?,?,?)")
        );


        env.execute();
    }
}
