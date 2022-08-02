package com.atguig.gmall.test;

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
 * Date: 2022/7/7
 * Desc: 该案例演示了Flink的intervaljoin
 */
public class Test01_intervalJoin {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        //TODO 2.准备员工流数据
        SingleOutputStreamOperator<Emp> empDS = env
            .socketTextStream("hadoop102", 8888)
            .map(
                lineStr -> {
                    String[] fieldArr = lineStr.split(",");
                    return new Emp(Integer.valueOf(fieldArr[0]), fieldArr[1], Integer.valueOf(fieldArr[2]), Long.valueOf(fieldArr[3]));
                }
            )
            .assignTimestampsAndWatermarks(
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
        empDS.print("emp:");

        //TODO 3.准备部门流数据
        SingleOutputStreamOperator<Dept> deptDS = env
            .socketTextStream("hadoop102", 8889)
            .map(
                lineStr -> {
                    String[] fieldArr = lineStr.split(",");
                    return new Dept(Integer.valueOf(fieldArr[0]), fieldArr[1], Long.valueOf(fieldArr[2]));
                }
            )
            .assignTimestampsAndWatermarks(
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

        deptDS.print("dept:");
        //TODO 4.使用intervaljoin将两条流进行关联
        SingleOutputStreamOperator<Tuple2<Emp, Dept>> joinedDS = empDS
            .keyBy(Emp::getDeptno)
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

        joinedDS.print(">>>>>");


        env.execute();
    }
}
