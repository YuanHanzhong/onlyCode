package com.atguigu.gmall.realtime.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.commons.lang3.StringUtils;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Author: Felix
 * Date: 2022/7/4
 * Desc: 流量域-用户跳出明细
 * 需要启动的进程
 *      zk、kafka、flume、DwdTrafficBaseLogSplit、DwdTrafficUserJumpDetail
 *
 */
public class DwdTrafficUserJumpDetail {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //TODO 2.检查点相关设置(略)
        //TODO 3.从kafka主题中读取数据
        //3.1 声明消费主题以及消费者组
        String topic = "dwd_traffic_page_log";
        String groupId = "dwd_traffic_user_jump_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

       /* DataStream<String> kafkaStrDS = env
            .fromElements(
                "{\"common\":{\"mid\":\"101\"},\"page\":{\"page_id\":\"home\"},\"ts\":10000} ",
                "{\"common\":{\"mid\":\"102\"},\"page\":{\"page_id\":\"home\"},\"ts\":12000}",
                "{\"common\":{\"mid\":\"102\"},\"page\":{\"page_id\":\"good_list\",\"last_page_id\":" +
                    "\"home\"},\"ts\":15000} ",
                "{\"common\":{\"mid\":\"102\"},\"page\":{\"page_id\":\"good_list\",\"last_page_id\":" +
                    "\"detail\"},\"ts\":30000} "
            );*/


        //TODO 4.对读取的数据进行类型转换   jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);
        //jsonObjDS.print(">>>>");
        //TODO 5.指定watermar以及提取事件时间字段
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = jsonObjDS.assignTimestampsAndWatermarks(
            //WatermarkStrategy.<JSONObject>forMonotonousTimestamps()
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
        //TODO 6.按照mid进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjWithWatermarkDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"));

        //TODO 7.使用FlinkCEP过滤跳出数据
        //7.1 定义pattern
        Pattern<JSONObject, JSONObject> pattern = Pattern.<JSONObject>begin("first").where(
            new SimpleCondition<JSONObject>() {
                @Override
                public boolean filter(JSONObject jsonObj) {
                    String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                    return StringUtils.isEmpty(lastPageId);
                }
            }
        ).next("second").where(
            new SimpleCondition<JSONObject>() {
                @Override
                public boolean filter(JSONObject jsonObj) {
                    String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                    return StringUtils.isEmpty(lastPageId);
                }
            }
        ).within(Time.seconds(10));
        //7.2 将pattern应用到流上
        PatternStream<JSONObject> patternDS = CEP.pattern(keyedDS, pattern);
        //7.3 从流中提取数据
        OutputTag<JSONObject> timeoutTag = new OutputTag<JSONObject>("timeoutTag") {};

        SingleOutputStreamOperator<JSONObject> matchDS = patternDS.flatSelect(
            timeoutTag,
            new PatternFlatTimeoutFunction<JSONObject, JSONObject>() {
                @Override
                public void timeout(Map<String, List<JSONObject>> map, long l, Collector<JSONObject> collector) throws Exception {
                    //提取的是超时数据
                    JSONObject jsonObj = map.get("first").get(0);
                    //注意：虽然使用的是collector的collect方法，但是数据放到参数1中指定的侧输出流中了
                    collector.collect(jsonObj);
                }
            },
            new PatternFlatSelectFunction<JSONObject, JSONObject>() {
                @Override
                public void flatSelect(Map<String, List<JSONObject>> map, Collector<JSONObject> collector) throws Exception {
                    //提取的是完全匹配的数据
                    JSONObject jsonObj = map.get("first").get(0);
                    //注意：这里将数据放到主流中
                    collector.collect(jsonObj);
                }
            }
        );

        //TODO 8.因为提取数据 一个是超时数据，一个是完全匹配数据，超时数据会通过侧输出流输出，完全匹配的数据通过主流输出
        // 所以我们这里需要将两条流合并  才能都得到完整的跳出明细数据
        DataStream<JSONObject> timeoutDS = matchDS.getSideOutput(timeoutTag);
        DataStream<JSONObject> unionDS = matchDS.union(timeoutDS);

        //TODO 9.将过滤出来的数据  发送到kafka的主题
        unionDS.print(">>>>");
        unionDS
            .map(jsonObj->jsonObj.toJSONString())
            .addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_user_jump_detail"));
        env.execute();
    }
}
