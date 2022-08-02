package com.atguigu.gmall.realtime.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.bean.UserRegisterBean;
import com.atguigu.gmall.realtime.util.MyDateFormatUtil;
import com.atguigu.gmall.realtime.util.MyClickHouseUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

/**

 * Desc: 用户域-用户注册聚合统计
 * 需要启动的进程
 *      zk、kafka、maxwell、clickhouse、DwdUserRegister、DwsUserUserRegisterWindow
 */
public class DwsUserUserRegisterWindow {
    public static void main(String[] args) throws Exception {
        // TODO 1. 环境准备
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        // TODO 2. 检查点相关设置(略)

        // TODO 3. 读取 Kafka dwd_user_register 主题数据，封装为流
        String topic = "dwd_user_register";
        String groupId = "dws_user_user_register_window";
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        DataStreamSource<String> source = env.addSource(kafkaConsumer);

        // TODO 4. 转换数据结构
        SingleOutputStreamOperator<JSONObject> jsonObjDS = source.map(JSON::parseObject);
        //{"create_time":"2022-07-08 11:17:04","user_id":"128","date_id":"2022-07-08","ts":"1657682224"}
        //jsonObjDS.print(">>>");

        // TODO 5. 设置水位线
        SingleOutputStreamOperator<JSONObject> withWatermarkDS = jsonObjDS.assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<JSONObject>forMonotonousTimestamps()
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<JSONObject>() {
                        @Override
                        public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                            return jsonObj.getLong("ts") * 1000L;
                        }
                    }
                )
        );

        //TODO 6.开窗
        AllWindowedStream<JSONObject, TimeWindow> windowDS
            = withWatermarkDS.windowAll(TumblingEventTimeWindows.of(Time.seconds(10)));

        //TODO 7.聚合操作
        SingleOutputStreamOperator<UserRegisterBean> aggregateDS = windowDS.aggregate(
            new AggregateFunction<JSONObject, Long, Long>() {
                @Override
                public Long createAccumulator() {
                    return 0L;
                }

                @Override
                public Long add(JSONObject value, Long accumulator) {
                    return ++accumulator;
                }

                @Override
                public Long getResult(Long accumulator) {
                    return accumulator;
                }

                @Override
                public Long merge(Long a, Long b) {
                    return null;
                }
            },
            new AllWindowFunction<Long, UserRegisterBean, TimeWindow>() {
                @Override
                public void apply(TimeWindow window, Iterable<Long> values, Collector<UserRegisterBean> out) throws Exception {
                    for (Long value : values) {
                        out.collect(new UserRegisterBean(
                            MyDateFormatUtil.toYmdHms(window.getStart()),
                            MyDateFormatUtil.toYmdHms(window.getEnd()),
                            value,
                            System.currentTimeMillis()
                        ));
                    }
                }
            }
        );

        //TODO 8.将聚合的结果写到ClickHouse中
        aggregateDS.print(">>>>");
        aggregateDS.addSink(
            MyClickHouseUtil.getJdbcSinkFunction("insert into dws_user_user_register_window values(?,?,?,?)")
        );

        env.execute();
    }
}
