package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.app.func.DimAsyncFunction;
import com.atguigu.gmall.realtime.bean.TradeSkuOrderBean;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import com.atguigu.gmall.realtime.util.TimestampLtz3CompareUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/*

 */
public class DwsTradeSkuOrderWindow_m_0715 {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        
        //TODO 2.检查点相关设置(略)
        
        //TODO 3.从kafka主题中读取数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_sku_order_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
        
        //TODO 4.对读取的数据进行类型转换   jsonStr->jsonObj
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON :: parseObject);

        /*{"create_time":"2022-06-22 09:22:50","sku_num":"1","split_original_amount":"3927.0000","sku_id":"34",
        "date_id":"2022-06-22","source_type_name":"用户查询","user_id":"101","province_id":"7","source_type_code":"2401",
        "row_op_ts":"2022-06-24 01:22:51.976Z","sku_name":"华为智慧屏V55i-J 55英寸 HEGE-550B 4K全面屏智能电视机 多方视频通话 AI升降摄像头 银钻灰 京品家电",
        "id":"590","order_id":"275","split_total_amount":"3927.0","ts":"1656033770"}*/
        //jsonObjDS.print(">>>>");
        
        //TODO 5.去重之前 按照唯一键 订单明细id进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjDS.keyBy(jsonObj -> jsonObj.getString("id"));
        
        //TODO 6.去重    Flink状态编程 + Flink定时器
        SingleOutputStreamOperator<JSONObject> distinctDS = keyedDS.process(
          new KeyedProcessFunction<String, JSONObject, JSONObject>() {
              //定义一个状态    用于存放当前组中进来的第一条数据
              private ValueState<JSONObject> lastValueState;
              
              @Override
              public void open(Configuration parameters) throws Exception {
                  lastValueState
                    = getRuntimeContext().getState(new ValueStateDescriptor<JSONObject>("lastValueState", JSONObject.class));
              }
              
              @Override
              public void processElement(JSONObject jsonObj, Context ctx, Collector<JSONObject> out) throws Exception {
                  //获取状态中的数据
                  JSONObject lastValue = lastValueState.value();
                  if (lastValue == null) {
                      //说明状态中还没有数据
                      //注册一个5秒后执行的定时器
                      long curTime = ctx.timerService().currentProcessingTime();
                      ctx.timerService().registerProcessingTimeTimer(curTime + 5000L);
                      //将当前对象放到状态中
                      lastValueState.update(jsonObj);
                  } else {
                      //如果状态中已经存在数据  说明是存在重复数据 需要进行去重
                      String lastRowOpTs = lastValue.getString("row_op_ts");
                      String curRotOpTs = jsonObj.getString("row_op_ts");
                      
                      if (TimestampLtz3CompareUtil.compare(lastRowOpTs, curRotOpTs) <= 0) {
                          lastValueState.update(jsonObj);
                      }
                  }
              }
              
              @Override
              public void onTimer(long timestamp, OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
                  JSONObject jsonObj = lastValueState.value();
                  if (jsonObj != null) {
                      out.collect(jsonObj);
                  }
                  lastValueState.clear();
              }
          }
        );
        
        //TODO 7.对去重之后的数据进行类型的转换    jsonObj->实体类对象  方便进行统计
        SingleOutputStreamOperator<TradeSkuOrderBean> tradeSkuOrderDS = distinctDS.map(
          new MapFunction<JSONObject, TradeSkuOrderBean>() {
              @Override
              public TradeSkuOrderBean map(JSONObject jsonObj) throws Exception {
                  TradeSkuOrderBean tradeSkuOrderBean
                    = TradeSkuOrderBean.builder()
                        .userId(jsonObj.getString("user_id"))
                        .orderIdSet(new HashSet<>(Collections.singleton(jsonObj.getString("order_id")))) // 1个订单下有多个订单明细, 用set合适 NOTE
                        .skuId(jsonObj.getString("sku_id"))
                        .orderUuCount(0L)
                        .originalAmount(jsonObj.getDouble("split_original_amount"))
                        .activityAmount(jsonObj.getDouble("split_activity_amount") == null ? 0.0 : jsonObj.getDouble("split_activity_amount"))
                        .couponAmount(jsonObj.getDouble("split_coupon_amount") == null ? 0.0 : jsonObj.getDouble("split_coupon_amount"))
                        .orderAmount(jsonObj.getDouble("split_total_amount"))
                        .ts(jsonObj.getLong("ts") * 1000)
                        .build();
                  
                  return tradeSkuOrderBean;
              }
          }
        );
        
        //TODO 8.判断是否为独立用户, 按照用户分组
        SingleOutputStreamOperator<TradeSkuOrderBean> uuTradeSkuOrderBeanDS
          = tradeSkuOrderDS
              .keyBy(TradeSkuOrderBean :: getUserId)
              .process(
                new KeyedProcessFunction<String, TradeSkuOrderBean, TradeSkuOrderBean>() {
                    private ValueState<String> lastOrderDateState; // State的, 后面加一个State标识, 非常好用
                  
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        // 有生存时间的话, 分开写比较清楚
                        ValueStateDescriptor<String> valueStateDescriptor = new ValueStateDescriptor<String>("lastOrderState", String.class);
                      
                        valueStateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(1)).build());
                        lastOrderDateState = getRuntimeContext().getState(valueStateDescriptor);
                    }
                  
                    @Override
                    public void processElement(TradeSkuOrderBean tradeSkuOrderBean, Context ctx, Collector<TradeSkuOrderBean> out) throws Exception {
                        String lastOrderDate = lastOrderDateState.value();
                        String currentOrderDate = DateFormatUtil.toDate(tradeSkuOrderBean.getTs());
                        if (StringUtils.isEmpty(lastOrderDate) || !lastOrderDate.equals(currentOrderDate)) {
                            tradeSkuOrderBean.setOrderUuCount(1L); // 独立用户计数
                            lastOrderDateState.update(lastOrderDate);
                        }
                      
                        // coolect 放在外面更好
                        out.collect(tradeSkuOrderBean);
                    }
                }
              );
        
        
        //TODO 10.加上水位线
        SingleOutputStreamOperator<TradeSkuOrderBean> tradeSkuOrderBeanWithWatermarkDS = uuTradeSkuOrderBeanDS.assignTimestampsAndWatermarks(
          WatermarkStrategy.<TradeSkuOrderBean>forBoundedOutOfOrderness(Duration.ofSeconds(3))
            .withTimestampAssigner(
              new SerializableTimestampAssigner<TradeSkuOrderBean>() {
                  @Override
                  public long extractTimestamp(TradeSkuOrderBean element, long recordTimestamp) {
                      return element.getTs();
                  }
              }
            )
        );
        
        
        //TODO 11.分组流
        KeyedStream<TradeSkuOrderBean, String> skuIdKeyedDS
          = tradeSkuOrderBeanWithWatermarkDS
              .keyBy(TradeSkuOrderBean :: getSkuId);
        
        //TODO 12，开窗流
        WindowedStream<TradeSkuOrderBean, String, TimeWindow> windowDS
          = skuIdKeyedDS
              .window(
                TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10))
              );
        
        
        // 13. 聚合
        //windowDS.reduce(
        //  new ReduceFunction<TradeSkuOrderBean>() {
        //      @Override
        //      public TradeSkuOrderBean reduce(TradeSkuOrderBean value1, TradeSkuOrderBean value2) throws Exception {
        //          return null;
        //      }
        //  },
        //  new WindowFunction<TradeSkuOrderBean, TradeSkuOrderBean, String, TimeWindow>() {
        //      @Override
        //      public void apply(String s, TimeWindow window, Iterable<TradeSkuOrderBean> input, Collector<TradeSkuOrderBean> out) throws Exception {
        //
        //      }
        //  }
        //
        //)
        
        
        //TODO 13.聚合
        SingleOutputStreamOperator<TradeSkuOrderBean> reduceDS = windowDS.reduce(
          new ReduceFunction<TradeSkuOrderBean>() {
              @Override
              public TradeSkuOrderBean reduce(TradeSkuOrderBean value1, TradeSkuOrderBean value2) throws Exception {
                  value1.getOrderIdSet().addAll(value2.getOrderIdSet()); // 集合的 addAll即可, 其他的值相加
                  value1.setOrderUuCount(value1.getOrderUuCount() + value2.getOrderUuCount());
                  value1.setOriginalAmount(value1.getOriginalAmount() + value2.getOriginalAmount());
                  value1.setActivityAmount(value1.getActivityAmount() + value2.getActivityAmount());
                  value1.setCouponAmount(value1.getCouponAmount() + value2.getCouponAmount());
                  value1.setOrderAmount(value1.getOrderAmount() + value2.getOrderAmount());
                  return value1;
                  
              }
          },
          new WindowFunction<TradeSkuOrderBean, TradeSkuOrderBean, String, TimeWindow>() {
              @Override // P3 窗口函数的练习
              public void apply(String s, TimeWindow window, Iterable<TradeSkuOrderBean> input, Collector<TradeSkuOrderBean> out) throws Exception {
                  for (TradeSkuOrderBean orderBean : input) {
                      orderBean.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                      orderBean.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));
                      orderBean.setTs(System.currentTimeMillis());
                      orderBean.setOrderCount((long) orderBean.getOrderIdSet().size());
                      out.collect(orderBean);
                  }
              }
          }
        );
        
        //reduceDS.print(">>>>");
        
        //TODO 14.把缺的维度补充上
        
        //14.1 关联SKU维度
        //将异步I/O操作应用于DataStream作为DataStream的一次转换操作
        /*
         2022/7/16 10:51 NOTE 异步和同步的理解 STAR
         异步 --- 不需要等待资源 --- 互不影响 --- 输入相同, 结果有可能不同
         同步 --- 需要等待资源   --- 排队     --- 输入相同, 结果一定相同
        */
        SingleOutputStreamOperator<TradeSkuOrderBean> withSkuInfoDS
          = AsyncDataStream.unorderedWait(
          reduceDS,
          //指定异步操作  实现分发请求的 AsyncFunction
          new DimAsyncFunction<TradeSkuOrderBean>("dim_sku_info") {
              @Override
              public void join(TradeSkuOrderBean tradeSkuOrderBean, JSONObject dimInfoJsonObj) {
                  //ID,SPU_ID,PRICE,SKU_NAME,SKU_DESC,WEIGHT,TM_ID,CATEGORY3_ID,SKU_DEFAULT_IMG,IS_SALE,CREATE_TIME
                  tradeSkuOrderBean.setSkuName(dimInfoJsonObj.getString("SKU_NAME"));
                  tradeSkuOrderBean.setTrademarkId(dimInfoJsonObj.getString("TM_ID"));
                  tradeSkuOrderBean.setCategory3Id(dimInfoJsonObj.getString("CATEGORY3_ID"));
                  tradeSkuOrderBean.setSpuId(dimInfoJsonObj.getString("SPU_ID"));
              }
              
              @Override
              public String getKey(TradeSkuOrderBean tradeSkuOrderBean) {
                  return tradeSkuOrderBean.getSkuId();
              }
          },
          60, TimeUnit.SECONDS
        );
        
        withSkuInfoDS.print("14.1 关联SKU维度-->");
        
        
        //14.2 关联SPU维度
        SingleOutputStreamOperator<TradeSkuOrderBean> withSpuInfoDS = AsyncDataStream.unorderedWait(
          withSkuInfoDS,
          new DimAsyncFunction<TradeSkuOrderBean>("dim_spu_info") {
              @Override
              public void join(TradeSkuOrderBean tradeSkuOrderBean, JSONObject dimInfoJsonObj) {
                  tradeSkuOrderBean.setSpuName(dimInfoJsonObj.getString("SPU_NAME"));
              }
              
              @Override
              public String getKey(TradeSkuOrderBean tradeSkuOrderBean) {
                  return tradeSkuOrderBean.getSpuId();
              }
          },
          60, TimeUnit.SECONDS
        );
        //withSpuInfoDS.print(">>>>");
        
        //14.3 关联TM维度
        SingleOutputStreamOperator<TradeSkuOrderBean> withTmDS = AsyncDataStream.unorderedWait(
          withSpuInfoDS,
          new DimAsyncFunction<TradeSkuOrderBean>("dim_base_trademark") {
              @Override
              public void join(TradeSkuOrderBean tradeSkuOrderBean, JSONObject dimInfoJsonObj) {
                  tradeSkuOrderBean.setTrademarkName(dimInfoJsonObj.getString("TM_NAME"));
              }
              
              @Override
              public String getKey(TradeSkuOrderBean tradeSkuOrderBean) {
                  return tradeSkuOrderBean.getTrademarkId();
              }
          }, 60, TimeUnit.SECONDS
        );
        
        //withTmDS.print(">>>>");
        // 14.4 关联三级分类表 base_category3
        SingleOutputStreamOperator<TradeSkuOrderBean> withCategory3Stream = AsyncDataStream.unorderedWait(
          withTmDS,
          new DimAsyncFunction<TradeSkuOrderBean>("dim_base_category3".toUpperCase()) {
              @Override
              public void join(TradeSkuOrderBean javaBean, JSONObject jsonObj) {
                  javaBean.setCategory3Name(jsonObj.getString("name".toUpperCase()));
                  javaBean.setCategory2Id(jsonObj.getString("category2_id".toUpperCase()));
              }
              
              @Override
              public String getKey(TradeSkuOrderBean javaBean) {
                  return javaBean.getCategory3Id();
              }
          },
          5 * 60, TimeUnit.SECONDS
        );
        
        // 14.5 关联二级分类表 base_category2
        SingleOutputStreamOperator<TradeSkuOrderBean> withCategory2Stream = AsyncDataStream.unorderedWait(
          withCategory3Stream,
          new DimAsyncFunction<TradeSkuOrderBean>("dim_base_category2".toUpperCase()) {
              @Override
              public void join(TradeSkuOrderBean javaBean, JSONObject jsonObj) {
                  javaBean.setCategory2Name(jsonObj.getString("name".toUpperCase()));
                  javaBean.setCategory1Id(jsonObj.getString("category1_id".toUpperCase()));
              }
              
              @Override
              public String getKey(TradeSkuOrderBean javaBean) {
                  return javaBean.getCategory2Id();
              }
          },
          5 * 60, TimeUnit.SECONDS
        );
        
        // 14.6 关联一级分类表 base_category1
        SingleOutputStreamOperator<TradeSkuOrderBean> withCategory1Stream = AsyncDataStream.unorderedWait(
          withCategory2Stream,
          new DimAsyncFunction<TradeSkuOrderBean>("dim_base_category1".toUpperCase()) {
              @Override
              public void join(TradeSkuOrderBean javaBean, JSONObject jsonObj) {
                  javaBean.setCategory1Name(jsonObj.getString("name".toUpperCase()));
              }
              
              @Override
              public String getKey(TradeSkuOrderBean javaBean) {
                  return javaBean.getCategory1Id();
              }
          },
          5 * 60, TimeUnit.SECONDS
        );
        
        
        //TODO 15.将和维度关联后的数据写到clickhouse中
        withCategory1Stream.print(">>>>>");
        withCategory1Stream.addSink(MyClickHouseUtil.getJdbcSink(
          "insert into dws_trade_sku_order_window values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
        ));
        env.execute();
    }
}
