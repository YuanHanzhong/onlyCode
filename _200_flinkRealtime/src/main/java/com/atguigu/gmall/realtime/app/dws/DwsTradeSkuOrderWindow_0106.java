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

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Author: Felix
 * Date: 2022/7/15
 * Desc: 交易域-sku粒度下单聚合统计
 * 需要启动的进程
 *      zk、kafka、maxwell、clickhouse、hdfs、hbase、Redis
 *      DwdTradeOrderPreProcess、DwdTradeOrderDetail、DwsTradeSkuOrderWindow
 * 开发流程
 *      基本环境准备
 *      检查点相关设置
 *      从kafka的下单事实表中读取下单数据
 *      对读取的内容进行转换  jsonStr->jsonObj
 *      去重
 *          产生重复原因：在DWD层构建订单预处理表的时候，订单明细和活动以及优惠券是左外连接
 *                      如果左表数据先来，这个时候会产生空数据以及重复数据
 *          去重思路：状态 + 定时器
 *                  去重前，按照唯一键进行分组；
 *                  将数据放到状态中，同时注册定时器；
 *                  再有数据过来的时候，和状态中存在数据进行时间的对比，将时间大的保留到状态中；
 *                  当定时器开始执行的时候，将状态中保存的数据发送到下游
 *     再次转化流中数据类型   jsonObj->实体类对象
 *     判断是否为下单独立用户----状态编程
 *     指定Watermark以及提取事件时间字段
 *     分组
 *     开窗
 *     聚合计算
 *     维度关联
 *          -基本实现
 *              在PhoenixUitl工具类封装了一个专门从Phoenix表中查询数据的方法
 *                  List<T> queryList(Connection conn,String sql,Class<T> clz)
 *              封装了专门用于查询维度的工具类 DimUtil
 *                  JSONObject getDimInfoNoCached(Connection conn,String tableName,Tuple2<String,String>...params)
 *          -优化1：旁路缓存
 *              思路：先从缓存中查询维度，如果缓存中找到了，直接将缓存中的维度作为返回值返回(缓存命中)；如果在缓存中没有找到对应的维度信息，
 *                  需要发送请求到Phoenix表中查询维度，并且将查询的结果放到缓存中缓存起来，下次直接从缓存中获取即可。
 *              缓存选型：
 *                  Flink状态: 性能好，但是只能当前进程中对其进行访问，操作性差
 *                  Redis:    性能也不错，操作方便
 *              注意：
 *                  缓存的数据应该设置expire，不能常驻Redis
 *                  一旦业务系统的维度数据发生了变化，缓存中的数据也应该清空
 *
 *          -优化2：异步IO
 *              在实时计算中，和外部系统交互能力直接影响实时的性能
 *              默认情况下，Flink在执行map等转化算子的时候，使用的是同步的方式，处理完一个元素后，再去处理另外一个元素。
 *              Flink提供了异步的处理方式
 *              先决条件：
 *                  交互数据库提供了异步操作的客户端
 *                  开启多个线程，发送异步请求
 *              具体步骤：
 *                  //将异步操作应用到流上
 *                  AsyncDataStream.[un]orderedWait(
 *                      流,
 *                      执行的异步操作 implement AsyncFunction,
 *                      超时时间,
 *                      时间单位
 *                  )
 *          抽取DimAsyncFunction extends RichAsyncFunction{
 *              asyncInvoke(){
 *                  //开启多个线程，发送异步请求，完成维度管理
 *              }
 *          }
 *
 * 模板方法设计模式
 *      在父类中定义实现某一个功能的核心算法的骨架(实现步骤)，具体的实现延迟到子类中去完成，
 *      在不改变父类核心算法骨架的前提下，每个子类都可以有自己不同的实现。
 */
public class DwsTradeSkuOrderWindow_0106 {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //TODO 2.检查点相关设置(略)
        //TODO 3.从kafka中读取数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_sku_order_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
        //TODO 4.对读取的数据进行类型转换
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);
        //{"create_time":"2022-07-08 09:30:55","sku_num":"1","split_original_amount":"5999.0000",
        // "sku_id":"3","date_id":"2022-07-08","source_type_name":"用户查询","user_id":"105",
        // "province_id":"34","source_type_code":"2401","row_op_ts":"2022-07-15 01:30:56.941Z",
        // "sku_name":"小米10 至尊纪念版 双模5G 骁龙865 120HZ高刷新率  游戏手机",
        // "id":"349","order_id":"175","split_total_amount":"5999.0","ts":"1657848655"}
        jsonObjDS.print("4.对读取的数据进行类型转换-->");

        //TODO 5. 去重前，按照唯一键进行分组   下单的唯一键是订单明细的id
        KeyedStream<JSONObject, String> keyedDS = jsonObjDS.keyBy(jsonObj -> jsonObj.getString("id"));

        //TODO 6.去重   状态 + 定时器
        SingleOutputStreamOperator<JSONObject> distinctDS = keyedDS.process(
            new KeyedProcessFunction<String, JSONObject, JSONObject>() {
                private ValueState<JSONObject> lastJsonObjState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastJsonObjState = getRuntimeContext().getState(new ValueStateDescriptor<JSONObject>("lastJsonObjState", JSONObject.class));
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<JSONObject> out) throws Exception {
                    //从状态中获取下单数据
                    JSONObject lastJsonObj = lastJsonObjState.value();
                    //判断状态中的数据是否存在
                    if (lastJsonObj == null) {
                        //说明当前这条数据是这个订单明细id对应的第一条数据
                        //注册定时器
                        long currentProcessingTime = ctx.timerService().currentProcessingTime();
                        ctx.timerService().registerProcessingTimeTimer(currentProcessingTime + 5000L);
                        //将数据维护到状态中
                        lastJsonObjState.update(jsonObj);
                    } else {
                        //说明当前这个订单明细id对应的数据重复，比较状态中保存的数据的时间和当前处理的时间
                        String lastRowOpTs = lastJsonObj.getString("row_op_ts");
                        String rowOpTs = jsonObj.getString("row_op_ts");
                        if (TimestampLtz3CompareUtil.compare(lastRowOpTs, rowOpTs) <= 0) {
                            //将当前数据更新到状态中
                            lastJsonObjState.update(jsonObj);
                        }
                    }
                }

                @Override
                public void onTimer(long timestamp, OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
                    //定时器触发 执行的方法
                    JSONObject jsonObj = lastJsonObjState.value();
                    if (jsonObj != null) {
                        //将状态中的数据 传递到下游
                        out.collect(jsonObj);
                        //清空状态
                        lastJsonObjState.clear();
                    }
                }
            }
        );

        // TODO 7. 转换数据结构
        SingleOutputStreamOperator<TradeSkuOrderBean> beanDS = distinctDS.map(
            new MapFunction<JSONObject, TradeSkuOrderBean>() {
                @Override
                public TradeSkuOrderBean map(JSONObject jsonObj) throws Exception {
                    String orderId = jsonObj.getString("order_id");
                    String userId = jsonObj.getString("user_id");
                    String skuId = jsonObj.getString("sku_id");
                    Double splitOriginalAmount = jsonObj.getDouble("split_original_amount");
                    Double splitActivityAmount = jsonObj.getDouble("split_activity_amount");
                    Double splitCouponAmount = jsonObj.getDouble("split_coupon_amount");
                    Double splitTotalAmount = jsonObj.getDouble("split_total_amount");
                    Long ts = jsonObj.getLong("ts") * 1000L;


                    TradeSkuOrderBean trademarkCategoryUserOrderBean = TradeSkuOrderBean.builder()
                        .orderIdSet(new HashSet<String>(
                        Collections.singleton(orderId)
                    ))
                        .skuId(skuId)
                        .userId(userId)
                        .orderUuCount(0L)
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

        //TODO 8.按照用户id进行分组
        KeyedStream<TradeSkuOrderBean, String> userKeyedDS = beanDS.keyBy(TradeSkuOrderBean::getUserId);
        //TODO 9.判断是否为下单独立用户
        SingleOutputStreamOperator<TradeSkuOrderBean> processDS = userKeyedDS.process(
            new KeyedProcessFunction<String, TradeSkuOrderBean, TradeSkuOrderBean>() {
                private ValueState<String> lastOrderDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    ValueStateDescriptor<String> valueStateDescriptor = new ValueStateDescriptor<>("lastOrderDateState", String.class);
                    valueStateDescriptor.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(1)).build());
                    this.lastOrderDateState  = getRuntimeContext().getState(valueStateDescriptor);
                }
                @Override
                public void processElement(TradeSkuOrderBean tradeSkuOrderBean, Context ctx, Collector<TradeSkuOrderBean> out) throws Exception {
                    //获取上次下单日期
                    String lastOrderDate = lastOrderDateState.value();
                    //获取当前下单日期
                    String curOrderDate = DateFormatUtil.toDate(tradeSkuOrderBean.getTs());
                    //如果上次下单日期为空，或者上次下单日期和当前下单日期不在同一天，说明是下单独立用户
                    if(StringUtils.isEmpty(lastOrderDate) || !lastOrderDate.equals(curOrderDate)){
                        tradeSkuOrderBean.setOrderUuCount(1L);
                        lastOrderDateState.update(curOrderDate);
                    }
                    out.collect(tradeSkuOrderBean);
                }
            }
        );
        //TODO 10.指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<TradeSkuOrderBean> withWatermarkDS = processDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<TradeSkuOrderBean>forMonotonousTimestamps()
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<TradeSkuOrderBean>() {

                        @Override
                        public long extractTimestamp(TradeSkuOrderBean javaBean, long recordTimestamp) {
                            return javaBean.getTs();
                        }
                    }
                )
        );
        //TODO 11.按照统计的维度sku进行分组
        KeyedStream<TradeSkuOrderBean, String> skuKeyedDS = withWatermarkDS.keyBy(TradeSkuOrderBean::getSkuId);

        //TODO 12.开窗
        WindowedStream<TradeSkuOrderBean, String, TimeWindow> windowDS = skuKeyedDS.window(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10)));

        //TODO 13.聚合计算
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
                    String stt = DateFormatUtil.toYmdHms(window.getStart());
                    String edt = DateFormatUtil.toYmdHms(window.getEnd());
                    for (TradeSkuOrderBean element : input) {
                        element.setStt(stt);
                        element.setEdt(edt);
                        element.setOrderCount((long) (element.getOrderIdSet().size()));
                        element.setTs(System.currentTimeMillis());
                        out.collect(element);
                    }
                }
            }
        );

        //TradeSkuOrderBean(stt=2022-07-15 14:16:50, edt=2022-07-15 14:17:00, trademarkId=null,
        // trademarkName=null, category1Id=null, category1Name=null, category2Id=null, category2Name=null,
        // category3Id=null, category3Name=null, orderIdSet=[223, 227], userId=4, skuId=11, skuName=null,
        // spuId=null, spuName=null, orderUuCount=1, orderCount=2, originalAmount=40985.0,
        // activityAmount=2400.0, couponAmount=0.0, orderAmount=38585.0, ts=1657865887990)
        //reduceDS.print(">>>>>");

        //TODO 14.和sku维度进行关联
        //将异步I/O操作应用于DataStream作为DataStream的一次转换操作
        SingleOutputStreamOperator<TradeSkuOrderBean> withSkuInfoDS = AsyncDataStream.unorderedWait(
            reduceDS,
            //异步操作 实现分发请求的 AsyncFunction
            new DimAsyncFunction<TradeSkuOrderBean>("dim_sku_info") {
                @Override
                public void join(TradeSkuOrderBean tradeSkuOrderBean, JSONObject dimInfoJsonObj) {
                    tradeSkuOrderBean.setSkuName(dimInfoJsonObj.getString("SKU_NAME"));
                    tradeSkuOrderBean.setSpuId(dimInfoJsonObj.getString("SPU_ID"));
                    tradeSkuOrderBean.setCategory3Id(dimInfoJsonObj.getString("CATEGORY3_ID"));
                    tradeSkuOrderBean.setTrademarkId(dimInfoJsonObj.getString("TM_ID"));
                }

                @Override
                public String getKey(TradeSkuOrderBean tradeSkuOrderBean) {
                    return tradeSkuOrderBean.getSkuId();
                }
            },
            60, TimeUnit.SECONDS
        );

        withSkuInfoDS.print("14.和sku维度进行关联-->");

        //TODO 15.和spu维度进行关联
        SingleOutputStreamOperator<TradeSkuOrderBean> withSpuDS = AsyncDataStream.unorderedWait(
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

        //TODO 16.和品牌tm维度进行关联
        SingleOutputStreamOperator<TradeSkuOrderBean> withTmDS = AsyncDataStream.unorderedWait(
            withSpuDS,
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
            60, TimeUnit.SECONDS
        );

        //TODO 16.和三级分类进行关联
        SingleOutputStreamOperator<TradeSkuOrderBean> withCategory3Stream = AsyncDataStream.unorderedWait(
            withTmDS,
            new DimAsyncFunction<TradeSkuOrderBean>("dim_base_category3".toUpperCase()) {
                @Override
                public void join(TradeSkuOrderBean javaBean, JSONObject jsonObj) {
                    javaBean.setCategory3Name(jsonObj.getString("NAME"));
                    javaBean.setCategory2Id(jsonObj.getString("CATEGORY2_ID"));
                }

                @Override
                public String getKey(TradeSkuOrderBean javaBean) {
                    return javaBean.getCategory3Id();
                }
            },
            60, TimeUnit.SECONDS
        );

        //TODO 17. 关联二级分类表 base_category2
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

        // TODO 18. 关联一级分类表 base_category1
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

        //TODO 19.将维度关联结果写到CK
        withCategory1Stream.print("19.将维度关联结果写到CK-->");
        withCategory1Stream.addSink(
            MyClickHouseUtil.getJdbcSink("insert into dws_trade_sku_order_window values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
        );

        env.execute();

    }
}
