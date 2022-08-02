package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.TrafficHomeDetailPageViewBean;
import com.atguigu.gmall.realtime.util.MyDateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
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
 * Date: 2022/7/12
 * Desc: 首页和详情页独立访客统计
 * 需要启动的进程
 *      zk、kafka、flume、clickhouse、DwdTrafficBaseLogSplit、DwsTrafficHomeDetailPageViewWindow
 */
public class DwsTrafficHomeDetailPageViewWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 设置流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);

        //TODO 2.检查点相关设置(略)
        //TODO 3.从kafka页面日志主题中读取
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_traffic_page_log";
        String groupId ="dws_traffic_home_detail_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据进行类型转换   jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //TODO 5.对数据进行过滤    保留首页和详情页的访问
        SingleOutputStreamOperator<JSONObject> filterDS = jsonObjDS.filter(
            new FilterFunction<JSONObject>() {
                @Override
                public boolean filter(JSONObject jsonObj) throws Exception {
                    String pageId = jsonObj.getJSONObject("page").getString("page_id");
                    return "home".equals(pageId) || "good_detail".equals(pageId);
                }
            }
        );
        //filterDS.print(">>>>");

        //TODO 6.设置Watermark以及提取事件时间字段
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = filterDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<JSONObject>() {
                        @Override
                        public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                            return jsonObj.getLong("ts");
                        }
                    }
                )
        );

        //TODO 7.按照mid进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjWithWatermarkDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"));

        //TODO 8.判断是否为首页和详情页独立访客
        SingleOutputStreamOperator<TrafficHomeDetailPageViewBean> processDS = keyedDS.process(
            new KeyedProcessFunction<String, JSONObject, TrafficHomeDetailPageViewBean>() {
                private ValueState<String> homeLastVisitDateState;
                private ValueState<String> detailLastVisitDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    ValueStateDescriptor<String> homeValueStateDescriptor = new ValueStateDescriptor<>("homeLastVisitDateState", String.class);
                    homeValueStateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(1)).build());
                    this.homeLastVisitDateState  = getRuntimeContext().getState(homeValueStateDescriptor);

                    ValueStateDescriptor<String> detailValueStateDescriptor = new ValueStateDescriptor<>("detailLastVisitDateState", String.class);
                    detailValueStateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(1)).build());
                    this.detailLastVisitDateState  = getRuntimeContext().getState(detailValueStateDescriptor);
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<TrafficHomeDetailPageViewBean> out) throws Exception {

                    String pageId = jsonObj.getJSONObject("page").getString("page_id");
                    String curVisitDate = MyDateFormatUtil.toDate(jsonObj.getLong("ts"));

                    Long homeUvCt = 0L;
                    Long detailUvCt = 0L;

                    if("home".equals(pageId)){
                        //如果访问的是首页  ,判断是否首页的独立访客
                        String homeLastVisitDate = homeLastVisitDateState.value();
                        if(StringUtils.isEmpty(homeLastVisitDate)||!curVisitDate.equals(homeLastVisitDate)){
                            homeUvCt = 1L;
                            homeLastVisitDateState.update(curVisitDate);
                        }
                    }

                    if("good_detail".equals(pageId)){
                        //如果访问的是详情页  ,判断是否详情页的独立访客
                        String detailLastVisitDate = detailLastVisitDateState.value();
                        if(StringUtils.isEmpty(detailLastVisitDate) || !curVisitDate.equals(detailLastVisitDate)){
                            detailUvCt = 1L;
                            detailLastVisitDateState.update(curVisitDate);
                        }
                    }

                    if(homeUvCt != 0L || detailUvCt != 0L){
                        out.collect(new TrafficHomeDetailPageViewBean(
                            "","",homeUvCt,detailUvCt,0L
                        ));
                    }
                }
            }
        );

        //TODO 9.开窗
        AllWindowedStream<TrafficHomeDetailPageViewBean, TimeWindow> windowDS = processDS.windowAll(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10)));

        //TODO 10.聚合计算
        SingleOutputStreamOperator<TrafficHomeDetailPageViewBean> reduceDS = windowDS.reduce(
            new ReduceFunction<TrafficHomeDetailPageViewBean>() {
                @Override
                public TrafficHomeDetailPageViewBean reduce(TrafficHomeDetailPageViewBean value1, TrafficHomeDetailPageViewBean value2) throws Exception {
                    value1.setHomeUvCt(value1.getHomeUvCt() + value2.getHomeUvCt());
                    value1.setGoodDetailUvCt(value1.getGoodDetailUvCt() + value2.getGoodDetailUvCt());
                    return value1;
                }
            },
            new AllWindowFunction<TrafficHomeDetailPageViewBean, TrafficHomeDetailPageViewBean, TimeWindow>() {
                @Override
                public void apply(TimeWindow window, Iterable<TrafficHomeDetailPageViewBean> values, Collector<TrafficHomeDetailPageViewBean> out) throws Exception {
                    for (TrafficHomeDetailPageViewBean pageViewBean : values) {
                        pageViewBean.setStt(MyDateFormatUtil.toYmdHms(window.getStart()));
                        pageViewBean.setEdt(MyDateFormatUtil.toYmdHms(window.getEnd()));
                        pageViewBean.setTs(System.currentTimeMillis());
                        out.collect(pageViewBean);
                    }
                }
            }
        );

        //TODO 11.将聚合的结果写到ClickHouse中
        reduceDS.print(">>>");
        reduceDS.addSink(
            MyClickHouseUtil.getJdbcSinkFunction("insert into dws_traffic_home_detail_page_view_window values(?,?,?,?,?)")
        );

        env.execute();
    }
}
