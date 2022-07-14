package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.api.common.state.ValueState;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
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
public class DwsTradeUserSpuOrderWindow_m_0714 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(4);
        
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_user_spu_order_window_group"; // p3 消费者组怎么回事, 两个组能消费1个数据吗
        
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        DataStreamSource<String> source = executionEnvironment.addSource(kafkaConsumer);
        source.print("source-->");
        // 2022/7/14 10:03
        // kafka中的都是kv json字符串, 只有一级
        // {
        //    "id": "385",
        //    "order_id": "190",
        //    "user_id": "97",
        //    "sku_id": "14",
        //    "sku_name": "华为 HUAWEI P40 麒麟990 5G SoC芯片 5000万超感知徕卡三摄 30倍数字变焦 6GB+128GB冰霜银全网通5G手机",
        //    "province_id": "16",
        //    "activity_id": null,
        //    "activity_rule_id": null,
        //    "coupon_id": null,
        //    "date_id": "2022-07-14",
        //    "create_time": "2022-07-14 16:01:32",
        //    "source_id": null,
        //    "source_type_code": "2403",
        //    "source_type_name": "智能推荐",
        //    "sku_num": "1",
        //    "split_original_amount": "4188.0000",
        //    "split_activity_amount": null,
        //    "split_coupon_amount": null,
        //    "split_total_amount": "4188.0",
        //    "ts": "1657785694",
        //    "row_op_ts": "2022-07-14 08:01:37.536Z"
        //}
    
        SingleOutputStreamOperator<JSONObject> jsonObjDStream = source.map(JSONObject :: parseObject); // 为空的过滤掉了
        KeyedStream<JSONObject, String> keyedJsonObjDS = jsonObjDStream.keyBy(jsonObject -> jsonObject.getString("id"));
        keyedJsonObjDS.print("keyedJsonObjDS-->");
        
        
        // {
        //    "create_time": "2022-07-14 16:01:32",
        //    "sku_num": "1",
        //    "split_original_amount": "4188.0000",
        //    "sku_id": "14",
        //    "date_id": "2022-07-14",
        //    "source_type_name": "智能推荐",
        //    "user_id": "97",
        //    "province_id": "16",
        //    "source_type_code": "2403",
        //    "row_op_ts": "2022-07-14 08:01:37.536Z",
        //    "sku_name": "华为 HUAWEI P40 麒麟990 5G SoC芯片 5000万超感知徕卡三摄 30倍数字变焦 6GB+128GB冰霜银全网通5G手机",
        //    "id": "385",
        //    "order_id": "190",
        //    "split_total_amount": "4188.0",
        //    "ts": "1657785694"
        //}
        
        
        // 使用process里的ontimer去重
    
        SingleOutputStreamOperator<JSONObject> processDS = keyedJsonObjDS.process(new KeyedProcessFunction<String, JSONObject, JSONObject>() {
            private ValueState<JSONObject> lastValueState;
        
            @Override
            public void open(Configuration parameters) throws Exception {
                lastValueState = getRuntimeContext().getState( // GOT 泛型很奇妙, 先写上lastValuseState才能正确补充上
                  new ValueStateDescriptor<JSONObject>("lastValueState", JSONObject.class)
                );
            }
        
            @Override
            public void processElement(JSONObject currentValue, Context ctx, Collector<JSONObject> out) throws Exception {
            
                // 第一次到来(value()为null), 注册定时器, 并更新状态值
                if (lastValueState.value() == null) {
                    long currentProcessingTime = ctx.timerService().currentProcessingTime();
                    ctx.timerService().registerProcessingTimeTimer(currentProcessingTime + 5000L);
                    lastValueState.update(currentValue);
                } else {
                    // 解决乱序的情况
                    String lastRowOpTs = lastValueState.value().getString("row_op_ts");
                    String currentRowOpTs = currentValue.getString("row_op_ts");
                    // 反正就是要大的
                    if (lastRowOpTs.compareTo(currentRowOpTs) < 0) {
                        lastValueState.update(currentValue);
                    }
                }
            
            }
        
            // 定时器一定会被触发, 因为第一次一定为null
            @Override
            public void onTimer(long timestamp, OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
            
                // 给个机会, 如果第二条数据过来比较及时, 就收集
                if (lastValueState.value() != null) { // NOTE 这里是 .value()
                    out.collect(lastValueState.value());
                }
            
                lastValueState.clear();
            }
        });
        
        processDS.print("processDS-->");
        
        executionEnvironment.execute();
    }
}
