package com.atguig.gmall.test;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * Author: Felix
 * Date: 2022/7/7
 * Desc: 该案例演示了从kafka、mysql中读取数据，并使用 lookupjoin 进行连接
 */
public class Test03_lookupJoin {
    public static void main(String[] args) {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(1);
        //1.3 指定表执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);


        //TODO 2.从kafka主题中读取Emp数据，创建动态表
        tableEnv.executeSql("CREATE TABLE emp (\n" +
            "  empno integer,\n" +
            "  ename string,\n" +
            "  deptno integer,\n" +
            " proc AS PROCTIME() " +
            ") WITH (\n" +
            "  'connector' = 'kafka',\n" +
            "  'topic' = 'first',\n" +
            "  'properties.bootstrap.servers' = 'hadoop102:9092',\n" +
            "  'properties.group.id' = 'testGroup',\n" +
            "  'scan.startup.mode' = 'latest-offset',\n" +
            "  'format' = 'json'\n" +
            ")");


        //TODO 3.从MySQL数据库中读取Dept数据，创建动态表
        tableEnv.executeSql("CREATE TABLE dept (\n" +
            "  deptno integer,\n" +
            "  dname STRING,\n" +
            "  ts bigint\n" +
            ") WITH (\n" +
            "   'connector' = 'jdbc',\n" +
            "   'url' = 'jdbc:mysql://hadoop102:3306/GMALL2022_config',\n" +
            "   'table-name' = 't_dept',\n" +
            "   'username' = 'root',\n" +
            "   'password' = 'root',\n" +
            "   'driver' = 'com.mysql.cj.jdbc.Driver'," +
            "   'lookup.cache.max-rows' = '500'," +
            "   'lookup.cache.ttl' = '1 hour'" +
            ")");

        //TODO 4.使用lookupjoin将两张表数据进行连接
        tableEnv.executeSql("select e.empno,e.ename,d.deptno,d.dname from emp e " +
            " join dept FOR SYSTEM_TIME AS OF e.proc AS d on e.deptno = d.deptno").print();

    }
}
