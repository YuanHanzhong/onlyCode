package com.atguigu.gmall.realtime.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

/**
 * Author: Felix
 * Date: 2022/7/4
 * Desc: 流量域-独立访客事务事实表
 * 需要启动的进程
 *      zk、kafka、flume、[hdfs]、DwdTrafficBaseLogSplit、DwdTrafficUniqueVisitorDetail
 * 执行流程
 *      运行模拟生成日志jar包
 *      将生成的日志数据落盘
 *      flume从磁盘文件上采集日志数据到kafka的topic_log主题中
 *      DwdTrafficBaseLogSplit从topic_log主题中读取日志数据,进行分流
 *          错误日志、启动日志、页面日志(dwd_traffic_page_log)、曝光日志、动作日志
 *      从kafka的页面日志主题中读取页面日志数据
 *      过滤出独立访客
 *      将独立访客明细写到kafka的主题中
 */
public class DwdTrafficUniqueVisitorDetail {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        /*
        //TODO 2.检查点相关设置
        //2.1 开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        //2.2 设置检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.3 设置取消job后，检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //2.4 设置两个检查点之间最小时间间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);
        //2.5 设置重启策略
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30),Time.seconds(3)));
        //2.6 设置状态后端
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/gmall/ck");
        //2.7 设置操作hadoop的用户
        System.setProperty("HADOOP_USER_NAME","atguigu");
        */
        //TODO 3.从kafka主题中读取页面日志数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_traffic_page_log";
        String groupId = "dwd_traffic_unique_visitor_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);

        new FlinkKafkaConsumer<String>("first",new SimpleStringSchema(),new Properties());
        //3.3 消费数据  封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据进行类型转换   jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //jsonObjDS.print(">>>>>>");

        //TODO 5.按照mid进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"));
        //TODO 6.使用Flink的状态编程  过滤独立访客
        SingleOutputStreamOperator<JSONObject> filterDS = keyedDS.filter(
            new RichFilterFunction<JSONObject>() {
                //定义一个状态，存放当前设备上次访问日期
                private ValueState<String> lastVisitDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    ValueStateDescriptor<String> valueStateDescriptor = new ValueStateDescriptor<>("lastVisitDateState", String.class);
                    //设置状态的生命周期
                    valueStateDescriptor.enableTimeToLive(
                        StateTtlConfig.newBuilder(Time.days(1))
                            //.setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                            //.setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                            .build()
                    );
                    lastVisitDateState
                        = getRuntimeContext().getState(valueStateDescriptor);
                }

                @Override
                public boolean filter(JSONObject jsonObj) throws Exception {
                    String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                    if(StringUtils.isNotEmpty(lastPageId)){
                        //说明当前这次访问是从其他页面跳转过来的，说明这次访问肯定不是独立访问,直接过滤掉
                        return false;
                    }

                    //获取上次访问日期
                    String lastVisitDate = lastVisitDateState.value();
                    //获取当前日期
                    String curVisitDate = DateFormatUtil.toDate(jsonObj.getLong("ts"));

                    //如果末次登录日期为 null 或者不是今日，则本次访问是该 mid 当日首次访问，保留数据，将末次登录日期更新为当日。
                    // 否则不是当日首次访问，丢弃数据。
                    if(StringUtils.isEmpty(lastVisitDate)||!lastVisitDate.equals(curVisitDate)){
                        lastVisitDateState.update(curVisitDate);
                        return true;
                    }
                    return false;
                }
            }
        );

        //TODO 7.将过滤出的独立访客写到kafka主题中
        filterDS.print(">>>>");
        filterDS
            .map(jsonObj->jsonObj.toJSONString())
            .addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_unique_visitor_detail"));

        env.execute();
    }
}
