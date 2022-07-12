package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.TrafficPageViewBean;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * Author: Felix
 * Date: 2022/5/30
 * Desc: 按照版本-渠道-地区-新老访客对访问指标(pv、dur、sv、uv、ujd)进行聚合
 * 需要启动的进程
 *      zk、kafka、flume、DwdTrafficBaseLogSplit、DwdTrafficUniqueVisitorDetail、DwdTrafficUserJumpDetail、DwsTrafficVcChArIsNewPageViewWindow
 */
public class DwsTrafficVcChArIsNewPageViewWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);

        //TODO 2.检查点相关设置(略)

        //TODO 3.从kafka主题中读取数据
        //3.1 声明消费主题以及消费者组
        String pvTopic = "dwd_traffic_page_log";
        String uvTopic = "dwd_traffic_unique_visitor_detail";
        String ujdTopic = "dwd_traffic_user_jump_detail";
        String groupId = "dws_traffic_vc_ch_ar_isnew_group";

        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> pvKafkaConsumer = MyKafkaUtil.getKafkaConsumer(pvTopic, groupId);
        FlinkKafkaConsumer<String> uvKafkaConsumer = MyKafkaUtil.getKafkaConsumer(uvTopic, groupId);
        FlinkKafkaConsumer<String> ujdKafkaConsumer = MyKafkaUtil.getKafkaConsumer(ujdTopic, groupId);

        //3.3 消费数据 封装为流
        DataStreamSource<String> pvStrDS = env.addSource(pvKafkaConsumer);
        DataStreamSource<String> uvStrDS = env.addSource(uvKafkaConsumer);
        DataStreamSource<String> ujdStrDS = env.addSource(ujdKafkaConsumer);

        //pvStrDS.print(">>>");
        //uvStrDS.print("###");
        //ujdStrDS.print("&&&");

        //TODO 4.对读取的数据进行类型的转换  jsonStr-->实体类对象
        //4.1  pageLog
        SingleOutputStreamOperator<TrafficPageViewBean> pvStatsDS = pvStrDS.map(new MapFunction<String, TrafficPageViewBean>() {
            @Override
            public TrafficPageViewBean map(String jsonStr) throws Exception {
                JSONObject jsonObj = JSON.parseObject(jsonStr);
                JSONObject commonJsonObj = jsonObj.getJSONObject("common");
                JSONObject pageJsonObj = jsonObj.getJSONObject("page");
                String lastPageId = pageJsonObj.getString("last_page_id");
                long svCt = 0L;
                if(lastPageId == null || lastPageId.length() == 0){
                    svCt = 1L;
                }
                TrafficPageViewBean trafficPageViewBean = new TrafficPageViewBean(
                    "",
                    "",
                    commonJsonObj.getString("vc"),
                    commonJsonObj.getString("ch"),
                    commonJsonObj.getString("ar"),
                    commonJsonObj.getString("is_new"),
                    0L,
                    svCt,
                    1L,
                    pageJsonObj.getLong("during_time"),
                    0L,
                    jsonObj.getLong("ts")
                );
                return trafficPageViewBean;
            }
        });
        //4.2  uv
        SingleOutputStreamOperator<TrafficPageViewBean> uvStatsDS = uvStrDS.map(
            new MapFunction<String, TrafficPageViewBean>() {
                @Override
                public TrafficPageViewBean map(String jsonStr) throws Exception {
                    JSONObject jsonObj = JSON.parseObject(jsonStr);
                    JSONObject commonJsonObj = jsonObj.getJSONObject("common");
                    return new TrafficPageViewBean(
                        "",
                        "",
                        commonJsonObj.getString("vc"),
                        commonJsonObj.getString("ch"),
                        commonJsonObj.getString("ar"),
                        commonJsonObj.getString("is_new"),
                        1L,0L,0L,0L,0L,
                        jsonObj.getLong("ts")
                    );
                }
            }
        );
        //4.3  ujd
        SingleOutputStreamOperator<TrafficPageViewBean> ujdStatsDS = ujdStrDS.map(
            new MapFunction<String, TrafficPageViewBean>() {
                @Override
                public TrafficPageViewBean map(String jsonStr) throws Exception {
                    JSONObject jsonObj = JSON.parseObject(jsonStr);
                    JSONObject commonJsonObj = jsonObj.getJSONObject("common");

                    return new TrafficPageViewBean(
                        "",
                        "",
                        commonJsonObj.getString("vc"),
                        commonJsonObj.getString("ch"),
                        commonJsonObj.getString("ar"),
                        commonJsonObj.getString("is_new"),
                        0L,0L,0L,0L,1L,jsonObj.getLong("ts")
                    );
                }
            }
        );


        //TODO 5.将3条流进行合并
        DataStream<TrafficPageViewBean> unionDS = pvStatsDS.union(
            uvStatsDS,
            ujdStatsDS
        );

        //TODO 6.指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<TrafficPageViewBean> trafficPageViewWithWatermarkDS = unionDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<TrafficPageViewBean>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<TrafficPageViewBean>() {
                        @Override
                        public long extractTimestamp(TrafficPageViewBean trafficPageViewBean, long recordTimestamp) {
                            return trafficPageViewBean.getTs();
                        }
                    }
                )
        );

        //TODO 7.分组
        KeyedStream<TrafficPageViewBean, Tuple4<String, String, String, String>> keyedDS = trafficPageViewWithWatermarkDS.keyBy(
            new KeySelector<TrafficPageViewBean, Tuple4<String, String, String, String>>() {
                @Override
                public Tuple4<String, String, String, String> getKey(TrafficPageViewBean trafficPageViewBean) throws Exception {
                    return Tuple4.of(
                        trafficPageViewBean.getVc(),
                        trafficPageViewBean.getAr(),
                        trafficPageViewBean.getCh(),
                        trafficPageViewBean.getIsNew()
                    );
                }
            }
        );

        //TODO 8.开窗
        WindowedStream<TrafficPageViewBean, Tuple4<String, String, String, String>, TimeWindow> windowDS
            = keyedDS.window(TumblingEventTimeWindows.of(Time.seconds(10))).allowedLateness(Time.seconds(15));


        //TODO 9.聚合计算
        SingleOutputStreamOperator<TrafficPageViewBean> reduceDS = windowDS.reduce(
            new ReduceFunction<TrafficPageViewBean>() {
                @Override
                public TrafficPageViewBean reduce(TrafficPageViewBean value1, TrafficPageViewBean value2) throws Exception {
                    value1.setUvCt(value1.getUvCt() + value2.getUvCt());
                    value1.setSvCt(value1.getSvCt() + value2.getSvCt());
                    value1.setPvCt(value1.getPvCt() + value2.getPvCt());
                    value1.setDurSum(value1.getDurSum() + value2.getDurSum());
                    value1.setUjCt(value1.getUjCt() + value2.getUjCt());
                    return value1;
                }
            },
            new WindowFunction<TrafficPageViewBean, TrafficPageViewBean, Tuple4<String, String, String, String>, TimeWindow>() {
                @Override
                public void apply(Tuple4<String, String, String, String> tuple4, TimeWindow window, Iterable<TrafficPageViewBean> input, Collector<TrafficPageViewBean> out) throws Exception {
                    for (TrafficPageViewBean trafficPageViewBean : input) {
                        trafficPageViewBean.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                        trafficPageViewBean.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));
                        trafficPageViewBean.setTs(System.currentTimeMillis());
                        out.collect(trafficPageViewBean);
                    }
                }
            }
        );

        reduceDS.print(">>>>");
        //TODO 10.将聚合的结果写到ClickHouse数据表中
        reduceDS.addSink(
            MyClickHouseUtil.<TrafficPageViewBean>getJdbcSink("insert into dws_traffic_vc_ch_ar_is_new_page_view_window values(?,?,?,?,?,?,?,?,?,?,?,?)")
        );


        env.execute();
    }
}
