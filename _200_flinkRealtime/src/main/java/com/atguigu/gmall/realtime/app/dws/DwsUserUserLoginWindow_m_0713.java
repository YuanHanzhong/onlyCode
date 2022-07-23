package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.UserLoginBean;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
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
 * Desc: 用户域--回流用户以及独立用户聚合统计
 */
public class DwsUserUserLoginWindow_m_0713 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);
        
        /*
         2022/7/13 10:05 STAR 先搭框架, 再填细节
        */
        //1. flink从kafka读取数据
        
        String topic = "dwd_traffic_page_log"; // 来自log
        String groupId = "dws_user_user_login_window"; // 随便起, 但是不能没有
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
        
        //2. 转换数据, 方便后续使用
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON :: parseObject);
        
        //3. 过滤出想要的数据
        //TODO 5. 过滤出登录的行为   只保留用户id 不为 null 且 last_page_id 为 null 或为 login 的数据
        SingleOutputStreamOperator<JSONObject> filterDS = jsonObjDS.filter(
          new FilterFunction<JSONObject>() {
              @Override
              public boolean filter(JSONObject jsonObj) throws Exception {
                  String uid = jsonObj.getJSONObject("common").getString("uid");
                  String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                  return StringUtils
                           .isNotEmpty(uid)
                           && ((StringUtils.isEmpty(lastPageId) || "login".equals(lastPageId)))
                    ;
              }
          }
        );
        
        //filterDS.print("filterDS---> "); //     * @return The closed DataStream.
        /*
         2022/7/13 11:16 GOT 一般有了print, 就直接关了, 不再往下传了. 不管是DS还是Table
        */
    
    
    
 
        
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = filterDS.assignTimestampsAndWatermarks(
          WatermarkStrategy
            .<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(3)) // 泛型是手动写上去的
            .withTimestampAssigner(
              new SerializableTimestampAssigner<JSONObject>() {
                  @Override
                  public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                      return jsonObj.getLong("ts");
                  }
              }
            )
        );
        
        //TODO 7.按照uid进行分组
        KeyedStream<JSONObject, String> keyedDS =
          jsonObjWithWatermarkDS
            .keyBy(
              jsonObj -> jsonObj.getJSONObject("common").getString("uid")
            );
        
        
        
        
        //TODO 8.判断是否为独立用户以及回流用户 ,如果是，创建实体对象进行计数(元组也行)
        // 使用Flink的状态编程
        SingleOutputStreamOperator<UserLoginBean> uuAndBackDS = keyedDS.process(
          new KeyedProcessFunction<String, JSONObject, UserLoginBean>() {
              //定义一个状态  用于存放上次登录日期
              private ValueState<String> lastLoginDateState;
              
              @Override
              public void open(Configuration parameters) throws Exception {
                  lastLoginDateState
                    = getRuntimeContext().getState(
                      new ValueStateDescriptor<String>("lastLoginDateState", String.class)
                  );
              }
              
              
              @Override
              public void processElement(JSONObject jsonObj, Context ctx, Collector<UserLoginBean> out) throws Exception {
                  //获取上次登录日期
                  String lastLoginDate = lastLoginDateState.value();
                  
                  //获取当前登录日期
                  Long ts = jsonObj.getLong("ts");
                  String curLoginDate = DateFormatUtil.toDate(ts);
                  
                  Long uuCt = 0L; // 登录用户
                  Long backCt = 0L; // 回流
                  
                  if (StringUtils.isNotEmpty(lastLoginDate)) { /*
                   2022/7/13 10:19 判断为空就用StringUtils GOT
                  */
                      //如果状态中的上次登录日期不为空，说明以前登录过，这次不是第一次登录
                      //判断上次登录日期和这次登录日期是不是同一天
                      if (!lastLoginDate.equals(curLoginDate)) {
                          //如果不是同一天
                          //独立用户计数
                          uuCt = 1L;
                          
                          
                          //判断上次登录日期和这次登录日期间隔时间是否大于等于8  如果是，回流用户计数
                          long days = (ts - DateFormatUtil.toTs(lastLoginDate)) / 1000 / 60 / 60 / 24; /*
                           2022/7/13 10:25 GOT 1000 / 60 / 60 / 24
                          */
                          if (days >= 8) {
                              backCt = 1L;
                          }
    
                          //将这次登录日期 更新到状态中
                          lastLoginDateState.update(curLoginDate);
                      }
                      //如果是同一天  不管是独立用户还是回流用户都是不计数的  默认值就是0，所以不需要做什么操作
                      
                      
                  } else {
                      //如果状态中的上次登录日期为空，说明这次是第一次登录  独立用户计数  将这次登录的日期更新到状态中
                      uuCt = 1L;
                      lastLoginDateState.update(curLoginDate);
                  }
                  
                  //
                  //if (uuCt != 0L || backCt != 0L) { // 只是为了好看, 回流一定是独立用户, 所以只需要判断独立即可
                  if (uuCt != 0L) { // 只是为了好看, 回流一定是独立用户, 所以只需要判断独立即可
                      out.collect(new UserLoginBean(
                        "",
                        "",
                        backCt,
                        uuCt,
                        0L
                      ));
                  }
              }
          }
        );
        
        //TODO 9.开窗
        /*
         2022/7/13 10:32 GOT 聚合之前一定先开窗, 再练习以下
        */
        AllWindowedStream<UserLoginBean, TimeWindow> windowDS
          = uuAndBackDS.windowAll(TumblingEventTimeWindows.of(Time.seconds(10)));
        
        //TODO 10.聚合计算, 跟sum类似
        // 类型一致, 用reduce, 更简单   GOT
        SingleOutputStreamOperator<UserLoginBean> reduceDS = windowDS.reduce(
          new ReduceFunction<UserLoginBean>() {
              @Override
              public UserLoginBean reduce(UserLoginBean value1, UserLoginBean value2) throws Exception {
                  value1.setBackCt(value1.getBackCt() + value2.getBackCt());
                  value1.setUuCt(value1.getUuCt() + value2.getUuCt());
                  return value1;
                  
              }
          },
          new AllWindowFunction<UserLoginBean, UserLoginBean, TimeWindow>() {
              @Override
              // PROCESS 更底层
              public void apply(TimeWindow window, Iterable<UserLoginBean> values, Collector<UserLoginBean> out) throws Exception {
                  // 补充属性
                  // DateFormat 神器
                  for (UserLoginBean userLoginBean : values) {
                      userLoginBean.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                      userLoginBean.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));
                      userLoginBean.setTs(System.currentTimeMillis());
                      out.collect(userLoginBean);
                  }
              }
          }
        );
        
        //TODO 11.将聚合的结果写到ClickHouse表中
        reduceDS.print("reduceDS---> "); // 流的可以有多个print, 动态表只能有1个print
        
        reduceDS.addSink(
          MyClickHouseUtil.getJdbcSink("insert into dws_user_user_login_window values(?,?,?,?,?)")
        );
        
        env.execute();
    }
}
