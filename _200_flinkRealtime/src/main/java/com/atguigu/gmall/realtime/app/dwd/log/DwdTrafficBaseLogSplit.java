package com.atguigu.gmall.realtime.app.dwd.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.util.DateFormatUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * Author: Felix
 * Date: 2022/5/20
 * Desc: 流量域未经加工的事务事实表
 * 对日志进行分流，将不同类型的日志经过分流之后放到kakfa的不同主题中，作为事实表
 *      错误日志---错误侧输出流
 *      启动日志---启动侧输出流
 *      曝光日志---曝光侧输出流
 *      动作日志---动作侧输出流
 *      页面日志---主流
 * 需要启动的进程
 *      zk、kafka、[hdfs]、flume(f1.sh)、DwdTrafficBaseLogSplit
 * 执行流程
 *      运行模拟生成日志数据jar包 lg.sh
 *      lg.sh运行之后，会将模拟生成的日志数据保存到hadoop102和hadoop103对应的磁盘文件上
 *      flume从指定的磁盘文件中读取日志数据，并将读取到的日志数据发送给kafka的主题topic_log
 *      DwdTrafficBaseLogSplit从kafka的topic_log中读取日志数据
 *          -对日志进行类型转换  jsonStr->jsonObj
 *          -如果在转换的过程中，有异常抛出，说明采集的日志数据不是标准的json格式，属于脏数据，发送到kafka专门存放脏数据的主题中
 *          -对新老访客标记进行修复---Flink状态编程
 *          -分流
 *          -将不同流的数据写到kafka的不同主题中
 */
public class DwdTrafficBaseLogSplit {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        /*
        //TODO 2.检查点相关的设置
        //2.1 开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        //2.2 设置检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.3 设置取消job后，检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //2.4 设置两个检查点之间最小时间间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);
        //2.5 设置重启策略
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30),Time.seconds(3)));
        //2.6 设置状态后端
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/xxxx");
        //2.7 设置操作hadoop的用户
        System.setProperty("HADOOP_USER_NAME","atguigu");
        */

        //TODO 3.从kafka主题中读取数据
        //3.1 声明消费的主题以及消费者组
        String topic = "topic_log";
        String groupId = "dwd_traffic_base_log_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> flinkKafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(flinkKafkaConsumer);

        //TODO 4.对流中数据类型进行转换  jsonStr-jsonObj  并进行简单的ETL,将脏数据放到侧输出流中
        //4.1 定义侧输出流标签
        OutputTag<String> dirtyTag = new OutputTag<String>("dirtyTag") {
        };
        //4.2 转换 清洗
        SingleOutputStreamOperator<JSONObject> cleanedDS = kafkaStrDS.process(
            new ProcessFunction<String, JSONObject>() {
                @Override
                public void processElement(String jsonStr, Context ctx, Collector<JSONObject> out) throws Exception {
                    try {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);
                        out.collect(jsonObj);
                    } catch (Exception e) {
                        //如果将字符串转换为json对象的时候，发生了异常，说明接收到的字符串不是一个标准的json，作为脏数据处理
                        //将脏数据放到侧输出流中
                        ctx.output(dirtyTag, jsonStr);
                    }
                }
            }
        );
        //cleanedDS.print(">>>>");
        //4.3 将脏数据写到kakfa主题中
        DataStream<String> dirtyDS = cleanedDS.getSideOutput(dirtyTag);
        //dirtyDS.print("$$$$");
        dirtyDS.addSink(MyKafkaUtil.getKafkaProducer("dirty_data"));

        //TODO 5.新老访客标记修复---Flink的状态编程
        //5.1 按照mid进行分组
        KeyedStream<JSONObject, String> keyedDS
            = cleanedDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"));

        //5.2 修复新老访客标记
        SingleOutputStreamOperator<JSONObject> fixedDS = keyedDS.map(
            new RichMapFunction<JSONObject, JSONObject>() {
                //注意：不能在声明的时候直接对状态进行初始化  因为这个时候还获取不到RuntimeContext
                private ValueState<String> lastVisitDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastVisitDateState
                        = getRuntimeContext().getState(new ValueStateDescriptor<String>("lastVisitDateState", String.class));
                }

                @Override
                public JSONObject map(JSONObject jsonObj) throws Exception {
                    String isNew = jsonObj.getJSONObject("common").getString("is_new");
                    String lastVisitDate = lastVisitDateState.value();
                    Long ts = jsonObj.getLong("ts");
                    String curVisitDate = DateFormatUtil.toYmdHms(ts);
                    if ("1".equals(isNew)) {
                        //前端标记为新访客
                        if (lastVisitDate == null || lastVisitDate.length() == 0) {
                            //说明是该设备第一次访问，将日志中 ts 对应的日期更新到状态中，不对 is_new 字段做修改
                            lastVisitDateState.update(curVisitDate);
                        } else {
                            if (!lastVisitDate.equals(curVisitDate)) {
                                //且首次访问日期不是当日，说明访问的是老访客，将 is_new 字段置为 0
                                isNew = "0";
                                jsonObj.getJSONObject("common").put("is_new", isNew);
                            }
                        }
                    } else {
                        //前端标记为老访客
                        if (lastVisitDate == null || lastVisitDate.length() == 0) {
                            //说明访问 APP 的是老访客但本次是该访客的页面日志首次进入程序,需要给状态补充一个曾经访问日期，我们这里设置为昨天
                            String yesterday = DateFormatUtil.toYmdHms(ts - 3600 * 1000 * 24);
                            lastVisitDateState.update(yesterday);
                        }
                    }
                    return jsonObj;
                }
            }
        );

        //fixedDS.print(">>>>>");

        //TODO 6.分流
        //6.1 定义侧输出流标签
        OutputTag<String> startTag = new OutputTag<String>("startTag") {
        };
        OutputTag<String> displayTag = new OutputTag<String>("displayTag") {
        };
        OutputTag<String> actionTag = new OutputTag<String>("actionTag") {
        };
        OutputTag<String> errTag = new OutputTag<String>("errTag") {
        };

        //6.2 分流逻辑实现
        SingleOutputStreamOperator<String> pageLogDS = fixedDS.process(
            new ProcessFunction<JSONObject, String>() {
                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<String> out) throws Exception {
                    //----判断是否有错误日志----
                    JSONObject errJsonObj = jsonObj.getJSONObject("err");
                    if (errJsonObj != null) {
                        //说明有错误日志   将错误日志发送到错误侧输出流
                        ctx.output(errTag, jsonObj.toString());
                        //从日志中将err属性删掉
                        jsonObj.remove("err");
                    }

                    //----判断是否为启动日志-----
                    JSONObject startJsonObj = jsonObj.getJSONObject("start");
                    if (startJsonObj != null) {
                        //启动日志  将启动日志发送到启动侧输出流
                        ctx.output(startTag, jsonObj.toJSONString());
                    } else {
                        //如果不是启动日志 都属于页面日志  只不过 在页面日志中有可能还包含了动作和曝光
                        //页面日志、动作日志、曝光日志  都应该包含如下属性  common、page、ts
                        JSONObject commonJsonObj = jsonObj.getJSONObject("common");
                        JSONObject pageJsonObj = jsonObj.getJSONObject("page");
                        Long ts = jsonObj.getLong("ts");

                        //----判断是否有曝光行为----
                        JSONArray displayArr = jsonObj.getJSONArray("displays");
                        if (displayArr != null) {
                            //遍历页面中的所有曝光行为
                            for (int i = 0; i < displayArr.size(); i++) {
                                //得到页面上的一条曝光信息
                                JSONObject displayJsonObj = displayArr.getJSONObject(i);
                                //创建新的json对象，用于存放曝光数据
                                JSONObject resDisplayJsonObj = new JSONObject();
                                resDisplayJsonObj.put("common", commonJsonObj);
                                resDisplayJsonObj.put("page", pageJsonObj);
                                resDisplayJsonObj.put("ts", ts);
                                resDisplayJsonObj.put("display", displayJsonObj);
                                //将曝光数据放到曝光侧输出流中
                                ctx.output(displayTag, resDisplayJsonObj.toJSONString());
                            }
                            jsonObj.remove("displays");
                        }

                        //----判断是否有动作-----
                        JSONArray actionArr = jsonObj.getJSONArray("actions");
                        if (actionArr != null) {
                            //对动作数组进行遍历
                            for (int i = 0; i < actionArr.size(); i++) {
                                //获取页面上一条动作信息
                                JSONObject actionJsonObj = actionArr.getJSONObject(i);
                                //创建一个新的json对象，用于存放动作数据
                                JSONObject resActionJsonObj = new JSONObject();
                                resActionJsonObj.put("common", commonJsonObj);
                                resActionJsonObj.put("page", pageJsonObj);
                                resActionJsonObj.put("action", actionJsonObj);
                                //将动作数据放到动作侧输出流中
                                ctx.output(actionTag, resActionJsonObj.toJSONString());
                            }
                            jsonObj.remove("actions");
                        }

                        //-----处理页面日志----
                        //将页面日志放到主流中
                        out.collect(jsonObj.toJSONString());
                    }
                }
            }
        );

        DataStream<String> errDS = pageLogDS.getSideOutput(errTag);
        DataStream<String> startDS = pageLogDS.getSideOutput(startTag);
        DataStream<String> displayDS = pageLogDS.getSideOutput(displayTag);
        DataStream<String> actionDS = pageLogDS.getSideOutput(actionTag);

        pageLogDS.print("pageLogDS>>>");
        errDS.print("errDS>>>");
        startDS.print("@@@");
        displayDS.print("###");
        actionDS.print("$$$$");


        //TODO 7.将不同流的数据写到kafka的不同的主题中  作为事实表
        pageLogDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_page_log"));
        errDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_error_log"));
        startDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_start_log"));
        displayDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_display_log"));
        actionDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_traffic_action_log"));

        env.execute();
    }
}
