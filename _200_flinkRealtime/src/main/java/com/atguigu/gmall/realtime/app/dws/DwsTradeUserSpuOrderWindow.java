package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.app.func.DimAsyncFunction;
import com.atguigu.gmall.realtime.bean.TradeUserSpuOrderBean;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import com.atguigu.gmall.realtime.util.TimestampLtz3CompareUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Desc: 交易域用户-SPU维度聚合统计
 * 需要启动的进程
 *      zk、kafka、maxwell、hdfs、hbase、redis、clickhouse、
 *      DwdTradeOrderDetail、DwdTradeOrderPreProcess、DwsTradeUserSpuOrderWindow
 */

public class DwsTradeUserSpuOrderWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //TODO 2.检查点相关设置(略)
        //TODO 3.从kafka的dwd_trade_order_detail主题中读取数据
        //3.1 声明消费的主题 以及消费者组
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_user_spu_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
        //source.print("source-->");
    
        // 2022/7/14 10:03
        // kafka中的都是kv json字符串, 只有一级
        // source-->:4>
        // {
        //      "id":"367",
        //      "order_id":"180",
        //      "user_id":"152",
        //      "sku_id":"1",
        //      "sku_name":"小米10 至尊纪念版 双模5G 骁龙865 120HZ高刷新率 120倍长焦镜头 120W快充 8GB+128GB 陶瓷黑 游戏手机",
        //      "province_id":"13",
        //      "activity_id":null,
        //      "activity_rule_id":null,
        //      "coupon_id":null,"date_id":"2022-07-14",
        //      "create_time":"2022-07-14 09:57:32",
        //      "source_id":null,
        //      "source_type_code":"2401",
        //      "source_type_name":"用户查询",
        //      "sku_num":"2",
        //      "split_original_amount":"11998.0000",
        //      "split_activity_amount":null,
        //      "split_coupon_amount":null,
        //      "split_total_amount":"11998.0",
        //      "ts":"1657763853",
        //      "row_op_ts":"2022-07-14 01:57:38.326Z"
        // }
        
        
        //TODO 4.对读取的数据进行类型转换  jsonStr->jsonObj

        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);
        //jsonObjDStream.print("jsonObjDStream-->");
    
        // 2022/7/14 10:07
        //jsonObjDStream-->:4>
        // {
        //      "create_time":"2022-07-14 09:57:32",
        //      "sku_num":"2",
        //      "split_original_amount":"11998.0000",
        //      "sku_id":"1",
        //      "date_id":"2022-07-14",
        //      "source_type_name":"用户查询",
        //      "user_id":"152",
        //      "province_id":"13",
        //      "source_type_code":"2401",
        //      "row_op_ts":"2022-07-14 01:57:38.326Z",
        //      "sku_name":"小米10 至尊纪念版 双模5G 骁龙865 120HZ高刷新率 120倍长焦镜头 120W快充 8GB+128GB 陶瓷黑 游戏手机",
        //      "id":"367",
        //      "order_id":"180",
        //      "split_total_amount":"11998.0",
        //      "ts":"1657763853"
        // }

        //TODO 5.按照订单明细id进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjDS.keyBy(jsonObj -> jsonObj.getString("id"));

        //TODO 6.去重
        SingleOutputStreamOperator<JSONObject> processDS = keyedDS.process(
            new KeyedProcessFunction<String, JSONObject, JSONObject>() {
                //定义一个状态  用于存放流中的下单数据
                private ValueState<JSONObject> lastValueState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastValueState
                        = getRuntimeContext().getState(new ValueStateDescriptor<JSONObject>("lastValueState", JSONObject.class));
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<JSONObject> out) throws Exception {
                    JSONObject lastValueJsonObj = lastValueState.value();

                    if (lastValueJsonObj == null) {
                        long currentProcessingTime = ctx.timerService().currentProcessingTime();
                        ctx.timerService().registerProcessingTimeTimer(currentProcessingTime + 5000L);
                        lastValueState.update(jsonObj);
                    } else {
                        //"row_op_ts":"2022-06-01 06:27:18.025Z"
                        String lastRowOpTs = lastValueJsonObj.getString("row_op_ts");
                        String curRowOpTs = jsonObj.getString("row_op_ts");
                        if (TimestampLtz3CompareUtil.compare(lastRowOpTs, curRowOpTs) <= 0) {
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

        //processDS.print(">>>>");
        //TODO 7.对流中类型进行转换      jsonObj->要统计聚合实体类对象
        SingleOutputStreamOperator<TradeUserSpuOrderBean> tradeUserSpuDS = processDS.map(
            new MapFunction<JSONObject, TradeUserSpuOrderBean>() {
                @Override
                public TradeUserSpuOrderBean map(JSONObject jsonObj) throws Exception {
                    String orderId = jsonObj.getString("order_id");
                    String userId = jsonObj.getString("user_id");
                    String skuId = jsonObj.getString("sku_id");
                    Double splitOriginalAmount = jsonObj.getDouble("split_original_amount");
                    Double splitActivityAmount = jsonObj.getDouble("split_activity_amount");
                    Double splitCouponAmount = jsonObj.getDouble("split_coupon_amount");
                    Double splitTotalAmount = jsonObj.getDouble("split_total_amount");
                    Long ts = jsonObj.getLong("ts") * 1000L;
                    TradeUserSpuOrderBean trademarkCategoryUserOrderBean = TradeUserSpuOrderBean.builder()
                        .orderIdSet(new HashSet<String>(
                            Collections.singleton(orderId)
                        ))
                        .userId(userId)
                        .skuId(skuId)
                        .originalAmount(splitOriginalAmount)
                        .activityAmount(splitActivityAmount == null ? 0.0 : splitActivityAmount)
                        .couponAmount(splitCouponAmount == null ? 0.0 : splitCouponAmount)
                        .orderAmount(splitTotalAmount)
                        .ts(ts)
                        .build();
                    return trademarkCategoryUserOrderBean;
                }
            }
        );

        //TODO 8.补充参与分组聚合的维度
       /* tradeUserSpuDS.map(
            new MapFunction<TradeUserSpuOrderBean, TradeUserSpuOrderBean>() {
                @Override
                public TradeUserSpuOrderBean map(TradeUserSpuOrderBean tradeUserSpuOrderBean) throws Exception {
                    String skuId = tradeUserSpuOrderBean.getSkuId();
                    DruidDataSource dataSource = DruidDSUtil.createDataSource();
                    DruidPooledConnection conn = dataSource.getConnection();
                    JSONObject dimInfoJsonObj = DimUtil.getDimInfo(conn,"dim_sku_info", Tuple2.of("id",skuId));
                    tradeUserSpuOrderBean.setSpuId(dimInfoJsonObj.getString("SPU_ID"));
                    tradeUserSpuOrderBean.setCategory3Id(dimInfoJsonObj.getString("CATEGORY3_ID"));
                    tradeUserSpuOrderBean.setTrademarkId(dimInfoJsonObj.getString("TM_ID"));
                    return tradeUserSpuOrderBean;
                }
            }
        );*/

        //将异步I/O操作应用于DataStream作为DataStream的一次转换操作
        SingleOutputStreamOperator<TradeUserSpuOrderBean> withSpuIdDS = AsyncDataStream.unorderedWait(
            tradeUserSpuDS,
            //实现分发请求的AsyncFunction
            new DimAsyncFunction<TradeUserSpuOrderBean>("dim_sku_info") {
                @Override
                public void join(TradeUserSpuOrderBean tradeUserSpuOrderBean, JSONObject dimInfoJsonObj) {
                    tradeUserSpuOrderBean.setSpuId(dimInfoJsonObj.getString("SPU_ID"));
                    tradeUserSpuOrderBean.setCategory3Id(dimInfoJsonObj.getString("CATEGORY3_ID"));
                    tradeUserSpuOrderBean.setTrademarkId(dimInfoJsonObj.getString("TM_ID"));
                }

                @Override
                public String getKey(TradeUserSpuOrderBean tradeUserSpuOrderBean) {
                    return tradeUserSpuOrderBean.getSkuId();
                }
            },
            60, TimeUnit.SECONDS
        );

        //withSpuIdDS.print(">>>");


        //TODO 9.指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<TradeUserSpuOrderBean> withWatermarkDS = withSpuIdDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<TradeUserSpuOrderBean>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<TradeUserSpuOrderBean>() {
                        @Override
                        public long extractTimestamp(TradeUserSpuOrderBean tradeUserSpuOrderBean, long recordTimestamp) {
                            return tradeUserSpuOrderBean.getTs();
                        }
                    }
                )
        );
        //TODO 10.按照统计的维度进行分组
        KeyedStream<TradeUserSpuOrderBean, String> userIdSpuIdKeyedDS = withWatermarkDS.keyBy(orderBean -> orderBean.getUserId() + "_" + orderBean.getSpuId());


        //TODO 11.开窗
        WindowedStream<TradeUserSpuOrderBean, String, TimeWindow> windowDS = userIdSpuIdKeyedDS.window(TumblingEventTimeWindows.of(Time.seconds(10)));

        //TODO 12.聚合计算
        SingleOutputStreamOperator<TradeUserSpuOrderBean> reduceDS = windowDS.reduce(
            new ReduceFunction<TradeUserSpuOrderBean>() {
                @Override
                public TradeUserSpuOrderBean reduce(TradeUserSpuOrderBean value1, TradeUserSpuOrderBean value2) throws Exception {
                    value1.getOrderIdSet().addAll(value2.getOrderIdSet());
                    value1.setOriginalAmount(value1.getOriginalAmount() + value2.getOriginalAmount());
                    value1.setActivityAmount(value1.getActivityAmount() + value2.getActivityAmount());
                    value1.setCouponAmount(value1.getCouponAmount() + value2.getCouponAmount());
                    value1.setOrderAmount(value1.getOrderAmount() + value2.getOrderAmount());
                    int size = value1.getOrderIdSet().size();
                    value1.setOrderCount(size);
                    return value1;

                }
            },
            new WindowFunction<TradeUserSpuOrderBean, TradeUserSpuOrderBean, String, TimeWindow>() {
                @Override
                public void apply(String s, TimeWindow window, Iterable<TradeUserSpuOrderBean> input, Collector<TradeUserSpuOrderBean> out) throws Exception {
                    for (TradeUserSpuOrderBean tradeUserSpuOrderBean : input) {
                        tradeUserSpuOrderBean.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                        tradeUserSpuOrderBean.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));
                        tradeUserSpuOrderBean.setTs(System.currentTimeMillis());
                        out.collect(tradeUserSpuOrderBean);
                    }
                }
            }
        );

       //TODO 13 .补充维度相关的属性
        //补充spu名称
        SingleOutputStreamOperator<TradeUserSpuOrderBean> withSpuDS = AsyncDataStream.unorderedWait(
            reduceDS,
            new DimAsyncFunction<TradeUserSpuOrderBean>("dim_spu_info") {
                @Override
                public void join(TradeUserSpuOrderBean tradeUserSpuOrderBean, JSONObject dimInfoJsonObj) {
                    tradeUserSpuOrderBean.setSpuName(dimInfoJsonObj.getString("SPU_NAME"));
                }

                @Override
                public String getKey(TradeUserSpuOrderBean tradeUserSpuOrderBean) {
                    return tradeUserSpuOrderBean.getSpuId();
                }
            },
            300, TimeUnit.SECONDS
        );

        // 13.2 关联品牌表 base_trademark
        SingleOutputStreamOperator<TradeUserSpuOrderBean> withTrademarkStream = AsyncDataStream.unorderedWait(
            withSpuDS,
            new DimAsyncFunction<TradeUserSpuOrderBean>("dim_base_trademark".toUpperCase()) {
                @Override
                public void join(TradeUserSpuOrderBean javaBean, JSONObject jsonObj) {
                    javaBean.setTrademarkName(jsonObj.getString("tm_name".toUpperCase()));
                }

                @Override
                public String getKey(TradeUserSpuOrderBean javaBean) {
                    return javaBean.getTrademarkId();
                }
            },
            5 * 60, TimeUnit.SECONDS
        );

        // 13.3 关联三级分类表 base_category3
        SingleOutputStreamOperator<TradeUserSpuOrderBean> withCategory3Stream = AsyncDataStream.unorderedWait(
            withTrademarkStream,
            new DimAsyncFunction<TradeUserSpuOrderBean>("dim_base_category3".toUpperCase()) {
                @Override
                public void join(TradeUserSpuOrderBean javaBean, JSONObject jsonObj) {
                    javaBean.setCategory3Name(jsonObj.getString("name".toUpperCase()));
                    javaBean.setCategory2Id(jsonObj.getString("category2_id".toUpperCase()));
                }

                @Override
                public String getKey(TradeUserSpuOrderBean javaBean) {
                    return javaBean.getCategory3Id();
                }
            },
            5 * 60, TimeUnit.SECONDS
        );

        // 13.4 关联二级分类表 base_category2
        SingleOutputStreamOperator<TradeUserSpuOrderBean> withCategory2Stream = AsyncDataStream.unorderedWait(
            withCategory3Stream,
            new DimAsyncFunction<TradeUserSpuOrderBean>("dim_base_category2".toUpperCase()) {
                @Override
                public void join(TradeUserSpuOrderBean javaBean, JSONObject jsonObj)  {
                    javaBean.setCategory2Name(jsonObj.getString("name".toUpperCase()));
                    javaBean.setCategory1Id(jsonObj.getString("category1_id".toUpperCase()));
                }

                @Override
                public String getKey(TradeUserSpuOrderBean javaBean) {
                    return javaBean.getCategory2Id();
                }
            },
            5 * 60, TimeUnit.SECONDS
        );

        // 13.5 关联一级分类表 base_category1
        SingleOutputStreamOperator<TradeUserSpuOrderBean> withCategory1Stream = AsyncDataStream.unorderedWait(
            withCategory2Stream,
            new DimAsyncFunction<TradeUserSpuOrderBean>("dim_base_category1".toUpperCase()) {
                @Override
                public void join(TradeUserSpuOrderBean javaBean, JSONObject jsonObj) {
                    javaBean.setCategory1Name(jsonObj.getString("name".toUpperCase()));
                }

                @Override
                public String getKey(TradeUserSpuOrderBean javaBean) {
                    return javaBean.getCategory1Id();
                }
            },
            5 * 60, TimeUnit.SECONDS
        );


        //TODO 14 .将聚合的结果写到CK中
        withCategory1Stream.print(">>>>");
        withCategory1Stream.addSink(
            MyClickHouseUtil.getJdbcSink("insert into dws_trade_user_spu_order_window values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
        );

        env.execute();
    }
}
