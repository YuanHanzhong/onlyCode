package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

/**
 * Desc: 交易域用户-SPU维度聚合统计
 * 需要启动的进程
 * zk、kafka、maxwell、hdfs、hbase、redis、clickhouse、
 * DwdTradeOrderDetail、DwdTradeOrderPreProcess、DwsTradeUserSpuOrderWindow
 */

    /*
     2022/7/13 22:54
     知识点
        去重
                // 2022/7/13 23:22 NOTE consumer 就是出
        实力
            先搭框架, 再填细节
            先写容易玩的地方
            报大片的红, 包导错了, 泛型写错了


        
    */
public class DwsTradeUserSpuOrderWindow_m_0714_v2 {
    // 17点36分
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(4);
        executionEnvironment.enableCheckpointing(3000L, CheckpointingMode.EXACTLY_ONCE);
        
        String topic = "dwd_trade_order_detail";
        String groupId = "dfasd";
        
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        
        SingleOutputStreamOperator<JSONObject> processedDS =
          executionEnvironment
            .addSource(kafkaConsumer)
            .map(JSONObject :: parseObject)
            .keyBy(r -> r.getString("id"))
            .process(
              new KeyedProcessFunction<String, JSONObject, JSONObject>() {
                  private ValueState<JSONObject> lastState;
                  
                  @Override
                  public void open(Configuration parameters) throws Exception {
                      lastState = getRuntimeContext().getState(
                        new ValueStateDescriptor<JSONObject>("lastState", JSONObject.class)
                      );
                  }
                  
                  @Override
                  public void processElement(JSONObject currentState, Context ctx, Collector<JSONObject> out) throws Exception {
                      
                      if (lastState.value() == null) {
                          lastState.update(currentState);
                          ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime());
                      } else {
                          if (lastState.value().getString("row_op_ts").compareTo(currentState.getString("row_op_ts")) < 0) {
                              lastState.update(currentState);
                          }
                      }
                  }
                  
                  @Override
                  public void onTimer(long timestamp, OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
                      if (lastState.value() != null) {
                          out.collect(lastState.value());
                      }
                      lastState.clear();
                  }
              }
            );
        
        processedDS.print("v2 --> ");
        
        
        executionEnvironment.execute();
    }
}
