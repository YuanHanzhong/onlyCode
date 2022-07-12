package com.atguigu.gmall.realtime.app.dwd.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternFlatTimeoutFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.Map;

/*
 * Author: Felix
 * Date: 2022/5/23
 * Desc: 流量域用户跳出明细事务事实表
 * 需要启动的进程
 *      zk、kafka、flume、[hdfs]、DwdTrafficBaseLogSplit、DwdTrafficUserJumpDetail
 * // 2022/7/7 8:58 NOTE 练习CEP编程
 */
public class DwdTrafficUserJumpDetail_m_0704 {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
       /* //TODO 2.检查点相关的设置
        //2.1 开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        //2.2 设置检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.3 job取消后检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //2.4 设置两个检查点之间最小时间间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);
        //2.5 设置重启策略
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30),Time.seconds(3)));
        //2.6 设置状态后端
        env.setStateBackend(new HashMapStateBackend());
        //env.getCheckpointConfig().setCheckpointStorage(new JobManagerCheckpointStorage());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/xxx");
        //2.7 设置 操作hadoop的用户
        System.setProperty("HADOOP_USER_NAME","atguigu");
        */
        //TODO 3.从kafka中读取数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_traffic_page_log";
        String groupId = "dwd_traffic_user_jump_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装流
       DataStream<String> kafkaStrDS = env
            .fromElements(
                "{\"common\":{\"mid\":\"101\"},\"page\":{\"page_id\":\"home\"},\"ts\":10000} ",
                "{\"common\":{\"mid\":\"101\"},\"page\":{\"page_id\":\"home\"},\"ts\":10000} ",
                "{\"common\":{\"mid\":\"102\"},\"page\":{\"page_id\":\"home\"},\"ts\":12000}",
                "{\"common\":{\"mid\":\"102\"},\"page\":{\"page_id\":\"good_list\",\"last_page_id\":" +
                    "\"home\"},\"ts\":15000} ",
                "{\"common\":{\"mid\":\"102\"},\"page\":{\"page_id\":\"good_list\",\"last_page_id\":" +
                    "\"detail\"},\"ts\":30000} "
            );

        //DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
        //TODO 4.对读取的数据进行类型转换
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //jsonObjDS.print(">>>");
// 2022/7/4 15:39 NOTE 乱序程度为0, 就成了mono
        //TODO 5.指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = jsonObjDS.assignTimestampsAndWatermarks(
            //WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(3))
            WatermarkStrategy
                .<JSONObject>forMonotonousTimestamps()
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<JSONObject>() {
                        @Override
                        public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                            return jsonObj.getLong("ts");
                        }
                    }
                )
        );

        //TODO 6.按照mid进行分组
        KeyedStream<JSONObject, String> keyedDS
            = jsonObjWithWatermarkDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"));

        //keyedDS.print("$$$$");

        //TODO 7.使用FlinkCEP过滤出跳出数据
        //7.1 定义pattern
        Pattern<JSONObject, JSONObject> pattern = Pattern.<JSONObject>begin("first").where(
            new SimpleCondition<JSONObject>() {
                @Override
                public boolean filter(JSONObject jsonObj) {
                    String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                    if(lastPageId == null || lastPageId.length() == 0){
                        return true;
                    }
                    return false;
                }
            }
        ).next("second").where(
            new SimpleCondition<JSONObject>() {
                @Override
                public boolean filter(JSONObject jsonObj) {
                    String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                    if(lastPageId == null || lastPageId.length() == 0){
                        return true;
                    }
                    return false;
                }
            }
        ).within(Time.seconds(3)); // 2022/7/4 16:07 NOTE 窗口大小

        //7.2 将pattern应用到流上
        PatternStream<JSONObject> patternDS = CEP.pattern(keyedDS, pattern);

        //7.3 从流中提取数据
        OutputTag<JSONObject> timeoutTag = new OutputTag<JSONObject>("timeoutTag") {};

        SingleOutputStreamOperator<JSONObject> filterDS = patternDS.flatSelect(
            timeoutTag,
            new PatternFlatTimeoutFunction<JSONObject, JSONObject>() {
                @Override
                public void timeout(Map<String, List<JSONObject>> map, long l, Collector<JSONObject> collector) throws Exception {
                    //处理超时数据  注意：虽然使用的是collector.collect向下游传递数据，但是我们是把数据放到了参数1中指定的侧输出流了
                    for (JSONObject jsonObj : map.get("first")) {
                        collector.collect(jsonObj);
                    }
                }
            },
            new PatternFlatSelectFunction<JSONObject, JSONObject>() {
                @Override
                public void flatSelect(Map<String, List<JSONObject>> map, Collector<JSONObject> collector) throws Exception {
                    //处理完全匹配的数据
                    List<JSONObject> jsonObjectList = map.get("first");
                    for (JSONObject jsonObj : jsonObjectList) {
                        collector.collect(jsonObj);
                    }
                }
            }
        );

        //TODO 8.将完全匹配数据和超时数据进行合并   主流：完全匹配数据   侧输出流：超时数据
        DataStream<JSONObject> unionDS = filterDS.union(
            filterDS.getSideOutput(timeoutTag)
        );

        //TODO 9.将合并后的流输出到kafka主题
        unionDS.print(">>>>");
        unionDS
            .map(jsonObj->jsonObj.toJSONString())
            .addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_user_jump_detail"));

        env.execute();

    }
}
