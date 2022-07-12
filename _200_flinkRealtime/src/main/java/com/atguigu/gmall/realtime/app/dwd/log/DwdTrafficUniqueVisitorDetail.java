package com.atguigu.gmall.realtime.app.dwd.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.api.common.functions.RichFilterFunction;
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

/**
 * Author: Felix
 * Date: 2022/5/21
 * Desc: 独立访客过滤, Unique Visitor, Daily Active Visitor
 * 需要启动的进程
 *      zk、kafka、[hdfs]、flume、DwdTrafficBaseLogSplit、DwdTrafficUniqueVisitorDetail
 * 执行流程
 *
 */
public class DwdTrafficUniqueVisitorDetail {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //TODO 2.检查点相关的设置(略)

        //TODO 3.从kafka主题中读取数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_traffic_page_log";
        String groupId = "dwd_unique_visitor_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.2 消费数据  封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据进行类型转换   jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //jsonObjDS.print(">>>>");

        //TODO 5.按照mid对数据进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"));

        //TODO 6.使用Flink状态编程过滤出独立访客
        SingleOutputStreamOperator<JSONObject> uvDS = keyedDS.filter(
            new RichFilterFunction<JSONObject>() {
                private ValueState<String> lastVisitDateState;
                @Override
                public void open(Configuration parameters) throws Exception {
                    ValueStateDescriptor<String> valueStateDescriptor
                        = new ValueStateDescriptor<>("lastVisitDateState", String.class);
                    valueStateDescriptor.enableTimeToLive(
                        StateTtlConfig.newBuilder(Time.days(1))
                            //.setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                            //.setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                            .build()
                    );
                    lastVisitDateState = getRuntimeContext().getState(valueStateDescriptor);
                }

                @Override
                public boolean filter(JSONObject jsonObj) throws Exception {
                    String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                    if (lastPageId != null && lastPageId.length() > 0) {
                        //说明是从其他页面跳转过来的，肯定不是独立访客
                        return false;
                    }

                    String lastVisitDate = lastVisitDateState.value();
                    String curVisitDate = DateFormatUtil.toYmdHms(jsonObj.getLong("ts"));
                    if (lastVisitDate != null && lastVisitDate.length() > 0 && lastVisitDate.equals(curVisitDate)) {
                        //说明今天访问过了
                        return false;
                    } else {
                        //以前从来没有访问过
                        lastVisitDateState.update(curVisitDate);
                        return true;
                    }
                }
            }
        );

        uvDS.print(">>>");

        //TODO 7.将独立访客保存到kafka的主题中
        uvDS
            .map(jsonObj->jsonObj.toJSONString())
            .addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_unique_visitor_detail"));

        env.execute();
    }
}
