package test;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * Author: Felix
 * Date: 2022/5/24
 * Desc: 该案例主要演示FlinkAPI intervalJoin
 */
public class Test01_IntervalJoin {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Emp> empDS = env.socketTextStream("hadoop102", 8888).map(
            lineStr -> {
                String[] fieldArr = lineStr.split(",");
                return new Emp(
                    Integer.parseInt(fieldArr[0]),
                    fieldArr[1],
                    Integer.parseInt(fieldArr[2]),
                    Long.parseLong(fieldArr[3])
                );
            }
        ).assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<Emp>forMonotonousTimestamps()
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<Emp>() {
                        @Override
                        public long extractTimestamp(Emp emp, long recordTimestamp) {
                            return emp.getTs();
                        }
                    }
                )
        );

        empDS.print("emp:"); //10,wd,9
        SingleOutputStreamOperator<Dept> deptDS = env.socketTextStream("hadoop102", 8889).map(
            lineStr -> {
                String[] fieldArr = lineStr.split(",");
                return new Dept(
                    Integer.parseInt(fieldArr[0]),
                    fieldArr[1],
                    Long.parseLong(fieldArr[2])
                );
            }
        ).assignTimestampsAndWatermarks(
            WatermarkStrategy
                .<Dept>forMonotonousTimestamps()
                .withTimestampAssigner(
                    new SerializableTimestampAssigner<Dept>() {
                        @Override
                        public long extractTimestamp(Dept dept, long recordTimestamp) {
                            return dept.getTs();
                        }
                    }
                )
        );
        deptDS.print("dept:");//100,zsf,10,8

        SingleOutputStreamOperator<Tuple2<Emp, Dept>> joinedDS = empDS
            .keyBy(Emp::getDeptno) // 2022/7/7 10:24 NOTE  主要为了指定通过谁连接
            .intervalJoin(deptDS.keyBy(Dept::getDeptno))
            .between(Time.milliseconds(-5), Time.milliseconds(5))
            .process(
                new ProcessJoinFunction<Emp, Dept, Tuple2<Emp, Dept>>() {
                    @Override
                    public void processElement(Emp emp, Dept dept, Context ctx, Collector<Tuple2<Emp, Dept>> out) throws Exception {
                        out.collect(Tuple2.of(emp, dept));
                    }
                }
            );

        joinedDS.print(">>>>");

        env.execute();
    }
}
