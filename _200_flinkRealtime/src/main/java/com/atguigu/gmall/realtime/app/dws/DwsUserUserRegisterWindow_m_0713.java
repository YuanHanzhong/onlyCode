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
 * Desc: 用户域 用户注册数 聚合统计
 * 需要启动的进程
 * zk、kafka、maxwell、DwdUserRegister、DwsUserUserRegisterWindow
 * 主要练习aggregate
 */
public class DwsUserUserRegisterWindow_m_0713 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);
        
        // flink从kafka读数据
        String topic = "dwd_user_register";
        String groupId = "dws_user_user_register_window";
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
        
        
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON :: parseObject);
        jsonObjDS.print(">>>");// print后, 流就给关了, 但是下面的依然可用, 这里注释掉, 是为了
        
        
        //TODO 5.指定Watermark以及提取事件时间字段

    
        // GOT 水位线 <--> 很精确 <--> 单位为毫秒 <--> 时间语义 <--> 触发窗口执行
        // GOT barria <--> checkpoint <--> 故障恢复 <--> 触发存储状态
        // GOT binlog <--> 备份 <--> 不需要太频繁, 不需要太准确 <--> 单位为秒
        // NOTE <T> forBoundedOutofOrderness, 是有泛型的

    
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = jsonObjDS.assignTimestampsAndWatermarks(
          WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(3))
            .withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
                @Override
                public long extractTimestamp(JSONObject element, long recordTimestamp) {
                    return element.getLong("ts") * 1000;
                }
            })
        );
    
    
        //TODO 6.开窗有2种方式, 分别什么时候用?
        AllWindowedStream<JSONObject, TimeWindow> windowDS =
          jsonObjWithWatermarkDS.windowAll(
            TumblingEventTimeWindows.of(Time.seconds(10)) // waterMark需要触发, 所以需要执行2次模拟数据
          );
        
        //TODO 7.聚合计算, 窗口中为json, 最终想要个数字, 使用aggregate
        SingleOutputStreamOperator<UserRegisterBean> aggregateDS = windowDS.aggregate(
          new AggregateFunction<JSONObject, Long, Long>() {
              @Override
              public Long createAccumulator() {
                  return 0L; // 初始值
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
              public Long merge(Long a, Long b) { // 滑动/滚动不需要写merge,
                  // 会话窗口需要写merge, 不知道窗口大小, 所以不能确定元素放到哪里. 两个窗口交叉上了, 就需要合并下.
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
        aggregateDS.print("aggregateDS--->");// 有了这个, 就不再往后传了
        aggregateDS.addSink(
          MyClickHouseUtil.getJdbcSink("insert into dws_user_user_register_window values(?,?,?,?)")
        );
        env.execute();
    }
}
