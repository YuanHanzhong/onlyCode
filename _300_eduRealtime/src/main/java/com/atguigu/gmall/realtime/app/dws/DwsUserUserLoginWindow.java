package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.UserLoginBean;
import com.atguigu.gmall.realtime.util.MyDateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**

 * Desc: 用户域--回流用户数以及当日独立用户数聚合统计
 * 需要启动的应用程序
 *      zk、kafka、flume、clickhouse、DwdTrafficBaseLogSplit、DwsUserUserLoginWindow
 */
public class DwsUserUserLoginWindow {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);

        //TODO 2.检查点相关设置(略)

        //TODO 3.从kafka页面日志主题中读取数据
        //3.1 声明消费的主题以及消费者组
        String topic = "dwd_traffic_page_log";
        String groupId = "dws_user_login_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.2 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据进行类型转换   jsonStr->JSONObject
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //TODO 5.过滤出登录行为
        SingleOutputStreamOperator<JSONObject> filterDS = jsonObjDS.filter(
            new FilterFunction<JSONObject>() {
                @Override
                public boolean filter(JSONObject jsonObj) throws Exception {
                    String uid = jsonObj.getJSONObject("common").getString("uid");
                    String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                    return StringUtils.isNotEmpty(uid)
                        && (StringUtils.isEmpty(lastPageId) || "login".equals(lastPageId));
                }
            }
        );
        //filterDS.print(">>>>>");

        //TODO 6.指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermarkDS = filterDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<JSONObject>() {
                        @Override
                        public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                            return jsonObj.getLong("ts");
                        }
                    }
                )
        );

        //TODO 7.按照uid进行分组
        KeyedStream<JSONObject, String> keyedDS
            = jsonObjWithWatermarkDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("uid"));

        //TODO 8.使用Flink状态编程 判断是否为回流用户以及是否为当日独立用户
        SingleOutputStreamOperator<UserLoginBean> processDS = keyedDS.process(
            new KeyedProcessFunction<String, JSONObject, UserLoginBean>() {
                private ValueState<String> lastLoginDateState;

                @Override
                public void open(Configuration parameters) throws Exception {
                    lastLoginDateState
                        = getRuntimeContext().getState(new ValueStateDescriptor<String>("lastLoginDateState",String.class));
                }

                @Override
                public void processElement(JSONObject jsonObj, Context ctx, Collector<UserLoginBean> out) throws Exception {
                    //获取状态中的上次登录日期
                    String lastLoginDate = lastLoginDateState.value();
                    Long ts = jsonObj.getLong("ts");
                    String curLoginDate = MyDateFormatUtil.toDate(ts);
                    Long uuCt = 0L;
                    Long backCt = 0L;
                    if(StringUtils.isNotEmpty(lastLoginDate)){
                        //如果末次登陆日期不等于当天日期
                        if(!curLoginDate.equals(lastLoginDate)){
                            uuCt = 1L;
                            //如果当天日期与末次登陆日期之差大于等于8天则回流用户数backCt置为1
                            long days = (ts - MyDateFormatUtil.toTs(lastLoginDate)) / 1000 / 60 / 60 / 24;
                            if(days >= 8){
                                backCt = 1L;
                            }
                            lastLoginDateState.update(curLoginDate);
                        }
                    }else{
                        //如果状态中的末次登陆日期为 null，将 uuCt 置为 1，backCt 置为 0，并将状态中的末次登陆日期更新为当日。
                        uuCt = 1L;
                        lastLoginDateState.update(curLoginDate);
                    }

                    if(uuCt != 0L || backCt != 0L){
                        out.collect(new UserLoginBean("","",backCt,uuCt,0L));
                    }
                }
            }
        );
        //TODO 9.开窗
        AllWindowedStream<UserLoginBean, TimeWindow> windowDS = processDS.windowAll(TumblingEventTimeWindows.of(Time.seconds(10)));

        //TODO 10.聚合
        SingleOutputStreamOperator<UserLoginBean> reduceDS = windowDS.reduce(
            new ReduceFunction<UserLoginBean>() {
                @Override
                public UserLoginBean reduce(UserLoginBean value1, UserLoginBean value2) throws Exception {
                    value1.setUuCt(value1.getUuCt() + value2.getUuCt());
                    value1.setBackCt(value1.getBackCt() + value2.getBackCt());
                    return value1;
                }
            },
            new AllWindowFunction<UserLoginBean, UserLoginBean, TimeWindow>() {
                @Override
                public void apply(TimeWindow window, Iterable<UserLoginBean> values, Collector<UserLoginBean> out) throws Exception {
                    for (UserLoginBean userLoginBean : values) {
                        userLoginBean.setStt(MyDateFormatUtil.toYmdHms(window.getStart()));
                        userLoginBean.setEdt(MyDateFormatUtil.toYmdHms(window.getEnd()));
                        userLoginBean.setTs(System.currentTimeMillis());
                        out.collect(userLoginBean);
                    }
                }
            }
        );

        //TODO 11.将聚合的结果写到ClickHouse中
        reduceDS.print(">>>");
        reduceDS.addSink(MyClickHouseUtil.getJdbcSinkFunction("insert into dws_user_user_login_window values(?,?,?,?,?)"));

        env.execute();
    }
}
