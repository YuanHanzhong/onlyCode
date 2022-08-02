package com.atguig.gmall.test;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.Duration;

/**
 * Author: Felix
 * Date: 2022/7/7
 * Desc: 该案例演示了FlinkSQL中连接
 *  状态失效时间TTL的设置
 *                              左                       右
 *          内连接          OnCreateAndWrite        OnCreateAndWrite
 *          左外连接        OnReadAndWrite          OnCreateAndWrite
 *          右外连接        OnCreateAndWrite        OnReadAndWrite
 *          全外连接        OnReadAndWrite          OnReadAndWrite
 */
public class Test02_sqlJoin {
    public static void main(String[] args) {
        //TODO 1.基本环境准备
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        tableEnv.getConfig().setIdleStateRetention(Duration.ofSeconds(10));

        //TODO 2.准备员工流数据
        SingleOutputStreamOperator<Emp> empDS = env
            .socketTextStream("hadoop102", 8888)
            .map(
                lineStr -> {
                    String[] fieldArr = lineStr.split(",");
                    return new Emp(Integer.valueOf(fieldArr[0]), fieldArr[1], Integer.valueOf(fieldArr[2]), Long.valueOf(fieldArr[3]));
                }
            );


        //TODO 3.准备部门流数据
        SingleOutputStreamOperator<Dept> deptDS = env
            .socketTextStream("hadoop102", 8889)
            .map(
                lineStr -> {
                    String[] fieldArr = lineStr.split(",");
                    return new Dept(Integer.valueOf(fieldArr[0]), fieldArr[1], Long.valueOf(fieldArr[2]));
                }
            );

        //TODO 4.将流转换为临时视图
        tableEnv.createTemporaryView("emp",empDS);
        tableEnv.createTemporaryView("dept",deptDS);

        //TODO 5.内连接
        //FlinkSQL对于参与连接的两张表，维护了两个状态；默认情况下，这两个状态中的数据是永久的保存起来的
        //在生产环境中，应该设置状态的TTL，防止状态中的数据过大
        //tableEnv.executeSql("select e.empno,e.ename,d.deptno,d.dname from emp e join dept d on e.deptno = d.deptno").print();

        //TODO 6.左外连接
        /**
         * 当左表数据来到的时候，没有找到对应的右表数据，这个时候生成一条数据， 左表 + null ,标记为+I
         * 当右表数据来到的时候，能够和左表数据进行关联，这个时候会生成和上面一样的一条数据，左表+null，标记为-D
         * 同时在生成一条 左表 + 右表的数据，标记为+I
         * 这种动态表生成的流称之为回撤流
         */
        //tableEnv.executeSql("select e.empno,e.ename,d.deptno,d.dname from emp e left join dept d on e.deptno = d.deptno").print();

        //TODO 7.右外连接
        //tableEnv.executeSql("select e.empno,e.ename,d.deptno,d.dname from emp e right join dept d on e.deptno = d.deptno").print();

        //TODO 8.全外连接
        //tableEnv.executeSql("select e.empno,e.ename,d.deptno,d.dname from emp e full join dept d on e.deptno = d.deptno").print();


        //TODO 9.将左外连接结果写到kafka主题中
        tableEnv.executeSql("CREATE TABLE kafka_emp (\n" +
            "\tempno integer,\n" +
            "\tename string,\n" +
            "\tdeptno integer,\n" +
            "\tdname string,\n" +
            "PRIMARY KEY (empno) NOT ENFORCED" +
            ") WITH (\n" +
            "  'connector' = 'upsert-kafka',\n" +
            "  'topic' = 'first',\n" +
            "  'properties.bootstrap.servers' = 'hadoop102:9092',\n" +
            "  'key.format' = 'json',\n" +
            "  'value.format' = 'json'\n" +
            ")");
        tableEnv.executeSql("insert into kafka_emp select e.empno,e.ename,d.deptno,d.dname from emp e left join dept d on e.deptno = d.deptno");




    }
}
