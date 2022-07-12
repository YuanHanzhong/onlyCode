package com.atguigu.gmall.realtime.app.dwd.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.common.GmallConfig_m;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

/*
 * Author: Felix
 * Date: 2022/5/21
 * Desc: 流量域, 独立访客, 日活, Unique Visitor, Daily Active Visitor
 * 需要启动的进程
 *      zk、kafka、[hdfs]、flume、DwdTrafficBaseLogSplit、DwdTrafficUniqueVisitorDetail
 * 执行流程
 * // 2022/7/7 8:58 NOTE
 *
 *
 */
public class DwdTrafficUniqueVisitorDetail_m_0704 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);
        
        //TODO 2.检查点相关的设置
        // 2022/7/4 11:56 NOTE 关于时间的4条,
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        
        env.getCheckpointConfig().setCheckpointTimeout(60 * 1000L);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2 * 2000L);
    
        // 2022/7/4 11:56 NOTE 失败重启策略, 1条
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(3), Time.seconds(10)));
    
        // 2022/7/4 11:57 NOTE  关于存储的3条
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage(GmallConfig_m.CHECK_POINT_STORAGE);
        
        // 高可用有专门的地址 GOT 2022年7月4日
        // 某个API可以点进去 GOT 2022年7月4日
        //env.setDefaultSavepointDirectory("hdfs://hadoop102:8020/flinkReaktime/checkPoint");
        
        
        System.setProperty("HADOOP_USER_NAME", "atguigu");
        
        //TODO 3.从kafka主题中读取数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_traffic_page_log";
        String groupId = "dwd_unique_visitor_group";
        //3.2 创建消费者对象
        /*
        2022/7/4 9:32 NOTE TODO 工具类值得多写
        
        
        */
        
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
        
        //TODO 4.对读取的数据进行类型转换, 方便处理   jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON :: parseObject);
        
        jsonObjDS.print("jsonObjDS>>>>");
        
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
                /*
                2022/7/4 9:11 NOTE 状态存活时间


                */
                  valueStateDescriptor.enableTimeToLive(StateTtlConfig
                                                          .newBuilder(Time.days(1L))
                                                          .build()); // 不能new的时候, 就点
                  
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
                  if (lastVisitDate != null && lastVisitDate.length() > 0 && lastVisitDate.equals(curVisitDate)) { // 2022/7/4 11:19 NOTE  这里用StingUtils TODO
                  //if (!StringUtils.isEmpty(lastVisitDate) && lastVisitDate.equals(curVisitDate)) {
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
          .map(jsonObj -> jsonObj.toString())
          .addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_unique_visitor_detail"));
        
        env.execute();
    }
}


/*
{"common":{"ar":"440000","uid":"256","os":"Android 11.0","ch":"web","is_new":"0","md":"vivo iqoo3","mid":"mid_515581","vc":"v2.1.134","ba":"vivo"},"display":{"display_type":"query","item":"29","item_type":"sku_id","pos_id":4,"order":9},"page":{"page_id":"good_detail","item":"27","during_time":15507,"item_type":"sku_id","source_type":"query"},"ts":1592100005000}
*/
