package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.TradeUserSpuOrderBean;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import com.atguigu.gmall.realtime.util.TimestampLtz3CompareUtil;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.util.Collections;
import java.util.HashSet;

public class DwsTradeUserSpuOrderWindow_m_0714_v2 {
    public static void main(String[] args) throws Exception {
        // 去重
        // 开窗,
        // 定时器
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(4);
        
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_user_spu_group";
        
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        DataStreamSource<String> source = executionEnvironment.addSource(kafkaConsumer);
        SingleOutputStreamOperator<JSONObject> processedDS
          = source
              .map(
                new MapFunction<String, JSONObject>() {
                    @Override
                    public JSONObject map(String value) throws Exception {
                        return JSON.parseObject(value);
                    }
                }
              )
              .keyBy(r -> r.getString("id"))
              .process(new KeyedProcessFunction<String, JSONObject, JSONObject>() {
                  private ValueState<JSONObject> lastStatus;
                
                  @Override
                  public void open(Configuration parameters) throws Exception {
                      lastStatus = getRuntimeContext().getState(
                        new ValueStateDescriptor<JSONObject>("lastStatus", JSONObject.class)
                      );
                  }
                
                  @Override
                  public void processElement(JSONObject currentStatusValue, Context ctx, Collector<JSONObject> out) throws Exception {
                      // 第一次来
                      if (lastStatus.value() == null) {
                          lastStatus.update(currentStatusValue);
                          ctx.timerService().registerProcessingTimeTimer(5000L + ctx.timerService().currentProcessingTime());
                      }
                    
                      // 第二次来, 第三次来. 有可能有多个重复, 因为有多次关联
                      else {
                          // NOTE 注意, 有可能时间及其相近, 这也算重复,
                          // GOT 字符串类型的日期是最容易比较的. LTZ类型, 位数可能为2位, 3位
                        
                          // 能更新就更新, 因为后来的更有可能是新的, 所以需要加上 =
                          if (TimestampLtz3CompareUtil.compare(lastStatus.value().getString("row_op_ts"), currentStatusValue.getString("row_op_ts") )<= 0) {
                              lastStatus.update(currentStatusValue);
                          }
                      }
                  }
                
                  @Override
                  public void onTimer(long timestamp, OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
                      if (lastStatus.value() != null) {
                        
                          out.collect(lastStatus.value());
                      }
                      lastStatus.clear();
                  }
              });
        
        // 封装为bean, 为的是更方便操作, 就不用复制粘贴字符串了
        SingleOutputStreamOperator<TradeUserSpuOrderBean> mappedBean = processedDS.map(
          new MapFunction<JSONObject, TradeUserSpuOrderBean>() {
              @Override
              public TradeUserSpuOrderBean map(JSONObject jsonObj) throws Exception {
                  // 根据需求, 要什么, 这里就定义什么
                  String orderId = jsonObj.getString("order_id");
                  String userId = jsonObj.getString("user_id");
                  String skuId = jsonObj.getString("sku_id");
                  Double splitOriginalAmount = jsonObj.getDouble("split_original_amount");
                  Double splitActivityAmount = jsonObj.getDouble("split_activity_amount");
                  Double splitCouponAmount = jsonObj.getDouble("split_coupon_amount");
                  Double splitTotalAmount = jsonObj.getDouble("split_total_amount");
                  Long ts = jsonObj.getLong("ts") * 1000L; // 注意时间单位的转换
                  
                  return TradeUserSpuOrderBean.builder()
                           .orderIdSet(
                             new HashSet<String>(
                               Collections.singleton(orderId) // NOTE 这里用的是collections,
                             )
                           )
                           .userId(userId)
                           .skuId(skuId)
                           .originalAmount(splitOriginalAmount == null ? 0.0 : splitOriginalAmount)
                           // GOT 小技巧: 为空则填充0, 不为空, 则不用管
                           .activityAmount(splitActivityAmount == null ? 0.0 : splitActivityAmount)
                           .couponAmount(splitCouponAmount == null ? 0.0 : splitCouponAmount)
                           .orderAmount(splitTotalAmount == null ? 0.0 : splitTotalAmount)
                           .ts(ts)
                           .build();
              }
          }
        );
        
        /*
         2022/7/15 10:07 关联相关维度, 每次都去HBase, 比较慢. 做优化
         优化一:
         优化2: 使用异步IO
         
        */
        
        
        executionEnvironment.execute();
    }
}
