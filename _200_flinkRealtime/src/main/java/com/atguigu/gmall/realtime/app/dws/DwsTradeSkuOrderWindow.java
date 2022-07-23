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
  Author: Felix
  Date: 2022/6/24
  Desc: 交易域-sku粒度下单各窗口聚合统计
  需要启动的进程
  zk、kafka、maxwell、clickhouse、hdfs、hbase
  DwdTradeOrderPreProcess、DwdTradeOrderDetail、DwsTradeSkuOrderWindow
  需求
       统计交易域-按照sku分组对下单业务过程的相关指标进行聚合统计操作
  开发流程
       环境准备
       检查点相关设置
       从kafka的下单事务事实表中读取下单数据
       类型转换        jsonstr->jsonObj
       去重
           重复数据产生原因：在准备dwd事务事实表的时候，订单明细和订单明细活动以及优惠券使用左外连接，如果左表先来，右表数据后到，会产生重复数据
           去重思路： flink状态编程 + 定时器
           第一条数据过来的时候，先将其放到状态中，并注册定时器；第二条数据过来的时候，比较当前时间和状态中存在的数据的时间，保留时间大的
           定时器触发执行的时候，从状态中获取数据向下游传递
       类型转换
           jsonObj->实体类
       独立用户判断
           使用Flink的状态编程，如果状态中内容为空，或者状态中存在的日期和当前访问不在同一天，那么属于独立用户，进行计数
       指定watermark以及提取事件时间字段
       分组
       开窗
       聚合计算
       维度关联
           基本维度关联的实现
               PhoenixUtil-------List<T> queryList(Connection conn,String sql,Class clz)
               DimUtil-----------JSONObject getDimInfoNoCache(Connection conn,String tableName,Tuple2...params)
           优化1：旁路缓存
               DimUtil---------- JSONObject getDimInfo(Connection conn,String tableName,Tuple2...params)
           优化2：异步IO
               默认情况下，在执行map等相关的转换操作的时候，单个并行度上是同步处理的，处理完一个元素在处理下一个，效率比较低
               Flink提供发送异步请求的API
               //将异步操作应用到流上
               AsyncDataStream.[un]orderedWait(
                   流对象,
                   异步操作 需要实现AsyncFunction接口，重写其asyncInvoke方法,
                       class DimAsyncFunction extends RichAsyncFunction{
                           open{
                               初始化线程池对象;
                               初始化Druid连接池对象;
                           }
                           asyncInvoke{
                               开启多线程，发送异步请求，实现维度关联
                           }
                       }
                   超时时间,
                   时间单位
               )
               // 2022/7/16 14:15 NOTE 设计模式, P3 什么时候使用解决什么问题是最重要的
 */
public class DwsTradeSkuOrderWindow {
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
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        /*{"create_time":"2022-06-22 09:22:50","sku_num":"1","split_original_amount":"3927.0000","sku_id":"34",
        "date_id":"2022-06-22","source_type_name":"用户查询","user_id":"101","province_id":"7","source_type_code":"2401",
        "row_op_ts":"2022-06-24 01:22:51.976Z","sku_name":"华为智慧屏V55i-J 55英寸 HEGE-550B 4K全面屏智能电视机 多方视频通话 AI升降摄像头 银钻灰 京品家电",
        "id":"590","order_id":"275","split_total_amount":"3927.0","ts":"1656033770"}*/
    
    
        // 2022/7/16 20:07 NOTE  debug技巧: 尽量缩小范围
        jsonObjDS.print("4.  jsonStr->jsonObj>>>>"); // 2022/7/16 20:07 NOTE  会在这里卡3分钟以上, 电脑该重启了
    
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
                  TradeSkuOrderBean tradeSkuOrderBean = TradeSkuOrderBean.builder()
                                                          .userId(jsonObj.getString("user_id"))
                                                          .orderIdSet(new HashSet<>(Collections.singleton(jsonObj.getString("order_id"))))
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
        
        //TODO 8.在统计下单独立用户前  按照用户id进行分组
        KeyedStream<TradeSkuOrderBean, String> userIdKeyedDS = tradeSkuOrderDS.keyBy(TradeSkuOrderBean::getUserId);
        
        //TODO 9.使用Flink状态编程   判断是否为独立用户
        SingleOutputStreamOperator<TradeSkuOrderBean> uuTradeSkuOrderBeanDS = userIdKeyedDS.process(
          new KeyedProcessFunction<String, TradeSkuOrderBean, TradeSkuOrderBean>() {
              private ValueState<String> lastOrderDateState;
              
              @Override
              public void open(Configuration parameters) throws Exception {
                  ValueStateDescriptor<String> valueStateDescriptor = new ValueStateDescriptor<>("lastOrderDateState", String.class);
                  valueStateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(1)).build());
                  this.lastOrderDateState
                    = getRuntimeContext().getState(valueStateDescriptor);
              }
              
              @Override
              public void processElement(TradeSkuOrderBean tradeSkuOrderBean, Context ctx, Collector<TradeSkuOrderBean> out) throws Exception {
                  //从状态中获取上次下单日期
                  String lastOrderData = lastOrderDateState.value();
                  //获取当前下单日期
                  String curOrderDate = DateFormatUtil.toDate(tradeSkuOrderBean.getTs());
                  if (StringUtils.isEmpty(lastOrderData) || !lastOrderData.equals(curOrderDate)) {
                      tradeSkuOrderBean.setOrderUuCount(1L);
                      lastOrderDateState.update(curOrderDate);
                  }
                  out.collect(tradeSkuOrderBean);
              }
          }
        );
        
        //TODO 10.指定watermark以及提取事件时间字段
        SingleOutputStreamOperator<TradeSkuOrderBean> tradeSkuOrderBeanWithWatermarkDS = uuTradeSkuOrderBeanDS.assignTimestampsAndWatermarks(
          WatermarkStrategy
            .<TradeSkuOrderBean>forBoundedOutOfOrderness(Duration.ofSeconds(3))
            .withTimestampAssigner(
              new SerializableTimestampAssigner<TradeSkuOrderBean>() {
                  @Override
                  public long extractTimestamp(TradeSkuOrderBean element, long recordTimestamp) {
                      return element.getTs();
                  }
              }
            )
        );
        //TODO 11.分组
        KeyedStream<TradeSkuOrderBean, String> skuIdKeyedDS = tradeSkuOrderBeanWithWatermarkDS.keyBy(TradeSkuOrderBean::getSkuId);
        //TODO 12，开窗
        WindowedStream<TradeSkuOrderBean, String, TimeWindow> windowDS = skuIdKeyedDS.window(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10)));
        //TODO 13.聚合
        SingleOutputStreamOperator<TradeSkuOrderBean> reduceDS = windowDS.reduce(
          new ReduceFunction<TradeSkuOrderBean>() {
              @Override
              public TradeSkuOrderBean reduce(TradeSkuOrderBean value1, TradeSkuOrderBean value2) throws Exception {
                  value1.getOrderIdSet().addAll(value2.getOrderIdSet());
                  value1.setOrderUuCount(value1.getOrderUuCount() + value2.getOrderUuCount());
                  value1.setOriginalAmount(value1.getOriginalAmount() + value2.getOriginalAmount());
                  value1.setActivityAmount(value1.getActivityAmount() + value2.getActivityAmount());
                  value1.setCouponAmount(value1.getCouponAmount() + value2.getCouponAmount());
                  value1.setOrderAmount(value1.getOrderAmount() + value2.getOrderAmount());
                  return value1;
                  
              }
          },
          new WindowFunction<TradeSkuOrderBean, TradeSkuOrderBean, String, TimeWindow>() {
              @Override
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
        
        reduceDS.print("13聚合-->");
        
        //TODO 14.发送异步请求和维度进行关联
        //14.1 关联SKU维度
        //将异步I/O操作应用于DataStream作为DataStream的一次转换操作
        SingleOutputStreamOperator<TradeSkuOrderBean> withSkuInfoDS = AsyncDataStream.unorderedWait(
          reduceDS,
          //指定异步操作  实现分发请求的 AsyncFunction
          new DimAsyncFunction<TradeSkuOrderBean>("dim_sku_info") {
              @Override
              public void join(TradeSkuOrderBean tradeSkuOrderBean, JSONObject dimInfoJsonObj) {
                  //ID,SPU_ID,PRICE,SKU_NAME,SKU_DESC,WEIGHT,TM_ID,CATEGORY3_ID,SKU_DEFAULT_IMG,IS_SALE,CREATE_TIME
    
                  // 2022/7/16 20:54 NOTE 没有关联上可能是因为没有维度数据, debug本质就是不断缩小问题范围, 可以借助多个print
                  // 2022/7/16 20:56 NOTE 越简单越好
             
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
          600, // 2022/7/16 15:57 NOTE 这里改成了600, 原来是60
          TimeUnit.SECONDS
        );
        
        withSkuInfoDS.print("14.1关联SKU维度-->");
        //14.2关联SPU维度-->:2> TradeSkuOrderBean(stt=2022-07-16 16:11:20, edt=2022-07-16 16:11:30, trademarkId=null,
        // trademarkName=null, category1Id=null, category1Name=null, category2Id=null, category2Name=null, category3Id=null,
        // category3Name=null, orderIdSet=[309], userId=28, skuId=23, skuName=null, spuId=null, spuName=null, orderUuCount=1,
        // orderCount=1, originalAmount=40.0, activityAmount=0.0, couponAmount=0.0, orderAmount=40.0, ts=1657959689598)
        //从phoenix表中查询数据的sql:select * from gmall2022_REALTIME.dim_spu_info where ID='null'
        //在phoenix表中，没有找到对应的维度数据:select * from gmall2022_REALTIME.dim_spu_info where ID='null'
    
    
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
          600, // 2022/7/16 15:57 NOTE 超时了会报错
  
          TimeUnit.SECONDS
        );
        withSpuInfoDS.print("14.2关联SPU维度-->");
        
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
          },
          600, // 2022/7/16 15:57 NOTE 这里改成了600, 原来是60
  
          TimeUnit.SECONDS
        );
        
        withTmDS.print("14.4关联TM维度-->");
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
          //5 * 60,
          600, // 2022/7/16 15:57 NOTE 这里改成了600, 原来是60
  
          TimeUnit.SECONDS
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
          //5 * 60,
          600, // 2022/7/16 15:57 NOTE 这里改成了600, 原来是60
  
          TimeUnit.SECONDS
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
          //5 * 60,
          600, // 2022/7/16 15:57 NOTE 这里改成了600, 原来是60
          TimeUnit.SECONDS
        );
        
        
        //TODO 15.将和维度关联后的数据写到clickhouse中
        withCategory1Stream.print("15. clickhouse: ");
        withCategory1Stream.addSink(MyClickHouseUtil.getJdbcSink(
          "insert into dws_trade_sku_order_window values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"));
        env.execute();
    }
}
