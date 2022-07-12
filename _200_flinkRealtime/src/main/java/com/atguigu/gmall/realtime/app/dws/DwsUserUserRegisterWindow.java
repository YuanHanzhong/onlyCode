package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.UserRegisterBean;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * Author: Felix
 * Date: 2022/5/31
 * Desc: 用户域 用户注册数 聚合统计
 * 需要启动的进程
 *      zk、kafka、maxwell、DwdUserRegister、DwsUserUserRegisterWindow
 */
public class DwsUserUserRegisterWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //TODO 2.检查点相关设置(略)
        //TODO 3.从kafka的dwd_user_register主题中读取数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_user_register";
        String groupId = "dws_user_user_register_window";

        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据  封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据进行类型转换  jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);
        //jsonObjDS.print(">>>");

        //TODO 5.指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = jsonObjDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<JSONObject>() {
                        @Override
                        public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                            return jsonObj.getLong("ts") * 1000;
                        }
                    }
                )
        );

        //TODO 6.开窗
        AllWindowedStream<JSONObject, TimeWindow> windowDS = jsonObjWithWatermarkDS.windowAll(TumblingEventTimeWindows.of(Time.seconds(10)));

        //TODO 7.聚合计算
        SingleOutputStreamOperator<UserRegisterBean> aggregateDS = windowDS.aggregate(
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
            new AllWindowFunction<Long, UserRegisterBean, TimeWindow>() {
                @Override
                public void apply(TimeWindow window, Iterable<Long> values, Collector<UserRegisterBean> out) throws Exception {
                    for (Long value : values) {
                        out.collect(new UserRegisterBean(
                            DateFormatUtil.toYmdHms(window.getStart()),
                            DateFormatUtil.toYmdHms(window.getEnd()),
                            value,
                            System.currentTimeMillis()
                        ));
                    }
                }
            }
        );

        //TODO 8.将聚合结果写到ClickHouse中
        aggregateDS.print(">>>");
        aggregateDS.addSink(
            MyClickHouseUtil.getJdbcSink("insert into dws_user_user_register_window values(?,?,?,?)")
        );
        env.execute();
    }
}
