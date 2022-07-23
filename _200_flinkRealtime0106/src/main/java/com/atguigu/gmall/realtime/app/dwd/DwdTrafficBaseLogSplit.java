package com.atguigu.gmall.realtime.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * Author: Felix
 * Date: 2022/7/2
 * Desc: 流量域-日志数据分流
 * 需要启动的进程
 *      zk、kafka、flume、DwdTrafficBaseLogSplit
 * 执行流程
 *      模拟生成日志数据jar包运行
 *      将生成的日志数据落盘
 *      flume从磁盘采集日志到kafka的topic_log主题中
 *      DwdTrafficBaseLogSplit从topic_log主题中读取日志数据
 *          -ETL，将脏数据写到kafka主题中
 *          -对新老访客标记进行修复 --- 状态编程
 *          -分流 --侧输出流
 */
public class DwdTrafficBaseLogSplit {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        /*
        //TODO 2.检查点相关设置
        //2.1 开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        //2.2 设置检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.3 设置job取消后检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //2.4 设置两个检查点之间最小时间间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);
        //2.5 设置重启策略
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30),Time.seconds(3)));
        //2.6 设置状态后端
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/gmall/ck");
        //2.7 设置操作hadoop的用户
        System.setProperty("HADOOP_USER_NAME","atguigu");
        */

        //TODO 3.从kafka的topic_log主题中读取数据
        //3.1 声明消费的主题以及消费者组
        String topic = "topic_log";
        String groupId = "dwd_traffic_base_log_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //kafkaStrDS.print(">>>");

        //TODO 4.对读取的数据进行类型转换以及ETL
        //4.1 声明侧输出流标签
        OutputTag<String> dirtyTag = new OutputTag<String>("dirtyTag"){};
        //4.2 转换结构 将脏数据放到侧输出流中
        SingleOutputStreamOperator<JSONObject> cleanedDS = kafkaStrDS.process(
            new ProcessFunction<String, JSONObject>() {
                @Override
                public void processElement(String jsonStr, Context ctx, Collector<JSONObject> out) throws Exception {
                    try {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);
                        out.collect(jsonObj);
                    } catch (Exception e) {
                        //如果转换的过程中发生了异常，说明流中的字符串不是一个标准的json字符串，属于脏数据，我们放到侧输出流中
                        ctx.output(dirtyTag, jsonStr);
                    }
                }
            }
        );
        //4.3 将侧输出流中的脏数据写到kafka主题中
        DataStream<String> dirtyDS = cleanedDS.getSideOutput(dirtyTag);
        dirtyDS.addSink(MyKafkaUtil.getKafkaProducer("dirty_data"));

        //cleanedDS.print(">>>>");


        //TODO 5.对新老访客标记进行修复
        //5.1 按照mid进行分组
        KeyedStream<JSONObject, String> keyedDS = cleanedDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"));
        //5.2 使用Flink的状态编程 进行修复
        SingleOutputStreamOperator<JSONObject> fixedDS = keyedDS.process(
            new KeyedProcessFunction<String, JSONObject, JSONObject>() {
                //注意:不能在声明状态的时候直接对其进行初始化操作，因为这个时候还没有获取运行时上下文对象
                private ValueState<String> lastVisitDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastVisitDateState
                        = getRuntimeContext().getState(new ValueStateDescriptor<String>("lastVisitDateState",String.class));
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<JSONObject> out) throws Exception {
                    String isNew = jsonObj.getJSONObject("common").getString("is_new");
                    String lastVisitDate = lastVisitDateState.value();
                    Long ts = jsonObj.getLong("ts");

                    String curVisitDate = DateFormatUtil.toDate(ts);

                    if("1".equals(isNew)){
                        //前端标记为新访客
                        if(StringUtils.isEmpty(lastVisitDate)){
                            //如果状态中日期为空，说明当前设备以前从来没有访问过，将这次访问日期更新到状态中
                            lastVisitDateState.update(curVisitDate);
                        }else{
                            //状态不为空的情况  判断当前访问日期和状态中的日期是否相同
                            if(!curVisitDate.equals(lastVisitDate)){
                                //如果当前访问日期和状态中的日期不相等，说明在今天之前这个设备就已经访问过  我们需要将其修复为0，标记为老访客
                                isNew = "0";
                                jsonObj.getJSONObject("common").put("is_new",isNew);
                            }
                        }
                    }else {
                        //前端标记为老访客,判断状态中是否存在上次访问日期；如果不存在，说明日志第一次进入到数仓项目，我们需要在状态中添加一个日期，
                        //用于标记曾经访问访问过
                        if(StringUtils.isEmpty(lastVisitDate)){
                            String yesterDay = DateFormatUtil.toDate(ts - 24 * 60 * 60 * 1000);
                            lastVisitDateState.update(yesterDay);
                        }
                    }
                    out.collect(jsonObj);
                }
            }
        );

        //fixedDS.print(">>>>");

        //TODO 6.按照日志类型进行分流---侧输出流  错误日志--错误侧输出流    启动日志-启动侧输出流  曝光日志-曝光侧输出流  动作日志-动作侧输出流  页面日志-主流
        //6.1 定义侧输出流标签
        OutputTag<String> errTag = new OutputTag<String>("errTag") {};
        OutputTag<String> startTag = new OutputTag<String>("startTag") {};
        OutputTag<String> displayTag = new OutputTag<String>("displayTag") {};
        OutputTag<String> actionTag = new OutputTag<String>("actionTag") {};
        //6.2 分流
        SingleOutputStreamOperator<String> pageLogDS = fixedDS.process(
            new ProcessFunction<JSONObject, String>() {
                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<String> out) throws Exception {
                    //错误日志处理
                    JSONObject errJsonObj = jsonObj.getJSONObject("err");
                    if(errJsonObj != null){
                        ctx.output(errTag,jsonObj.toJSONString());
                        jsonObj.remove("err");
                    }
                    JSONObject startJsonObj = jsonObj.getJSONObject("start");
                    if(startJsonObj != null){
                        //启动日志处理
                        ctx.output(startTag,jsonObj.toString());
                    }else{
                        //页面日志处理  每个页面日志都存在common、page和ts属性，所以我们先获取这些属性的值
                        JSONObject commonJsonObj = jsonObj.getJSONObject("common");
                        JSONObject pageJsonObj = jsonObj.getJSONObject("page");
                        Long ts = jsonObj.getLong("ts");
                        //曝光日志处理
                        JSONArray displayArr = jsonObj.getJSONArray("displays");
                        if(displayArr != null){
                            for (int i = 0; i < displayArr.size(); i++) {
                                JSONObject displayJsonObj = displayArr.getJSONObject(i);
                                JSONObject returnDispalyJsonObj = new JSONObject();
                                returnDispalyJsonObj.put("common",commonJsonObj);
                                returnDispalyJsonObj.put("page",pageJsonObj);
                                returnDispalyJsonObj.put("ts",ts);
                                returnDispalyJsonObj.put("display",displayJsonObj);
                                ctx.output(displayTag,returnDispalyJsonObj.toJSONString());
                            }
                        }
                        //动作日志处理
                        JSONArray actionArr = jsonObj.getJSONArray("actions");
                        if(actionArr != null){
                            for (int i = 0; i < actionArr.size(); i++) {
                                JSONObject actionJsonObj = actionArr.getJSONObject(i);
                                JSONObject returnActionJsonObj = new JSONObject();
                                returnActionJsonObj.put("common",commonJsonObj);
                                returnActionJsonObj.put("page",pageJsonObj);
                                returnActionJsonObj.put("action",actionJsonObj);
                                ctx.output(actionTag,returnActionJsonObj.toJSONString());
                            }
                        }
                        jsonObj.remove("displays");
                        jsonObj.remove("actions");
                        out.collect(jsonObj.toJSONString());
                    }

                }
            }
        );
        DataStream<String> errDS = pageLogDS.getSideOutput(errTag);
        DataStream<String> startDS = pageLogDS.getSideOutput(startTag);
        DataStream<String> displayDS = pageLogDS.getSideOutput(displayTag);
        DataStream<String> actionDS = pageLogDS.getSideOutput(actionTag);

        //TODO 7.将不同流的数据写到kafka的不同的主题中
        pageLogDS.print(">>>");
        errDS.print("###");
        startDS.print("$$$");
        displayDS.print("@@");
        actionDS.print("&&&");

        pageLogDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_page_log"));
        errDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_err_log"));
        startDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_start_log"));
        displayDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_display_log"));
        actionDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_action_log"));

        env.execute();
    }
}
