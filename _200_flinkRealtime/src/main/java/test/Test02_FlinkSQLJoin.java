package test;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.Duration;

/**
 * Author: Felix
 * Date: 2022/5/24
 * Desc: 该案例主要演示FlinkSQL JOIN
 */
public class Test02_FlinkSQLJoin {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(1);
        //1.3 设置表执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
    
        // 2022/7/7 14:46 NOTE  设置失效时间 TODO 查询也更新失效时间怎么设置
        tableEnv.getConfig().setIdleStateRetention(Duration.ofSeconds(10));
        //tableEnv.getConfig().set;
        
        
        //TODO 2.从指定的端口读取数据  得到流
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
        );

        SingleOutputStreamOperator<Dept> deptDS = env.socketTextStream("hadoop102", 8889).map(
            lineStr -> {
                String[] fieldArr = lineStr.split(",");
                return new Dept(
                    Integer.parseInt(fieldArr[0]),
                    fieldArr[1],
                    Long.parseLong(fieldArr[2])
                );
            }
        );
        //TODO 3.将流转换为动态表
        tableEnv.createTemporaryView("emp",empDS);
        tableEnv.createTemporaryView("dept",deptDS);

        //TODO 4.内连接        左表:OnCreateAndWrite    右表:OnCreateAndWrite,
        //tableEnv.executeSql("select e.empno,e.ename,e.deptno,d.dname from emp e join dept d on e.deptno = d.deptno") .print();

        //TODO 5.左外连接       左表:OnReadAndWrite    右表:OnCreateAndWrite,
        //假设有两张表进行左外连接，左表数据过来，如果右表数据还未到，会生成一条右表字段为null的数据，标记为+I
        //然后右表的数据到了，会将之前的数据撤回，会生成一条和第一条数据内容相同，但是标记为-D的数据
        //再生成一条关联后的数据，标记为+I
        tableEnv.executeSql("select e.empno,e.ename,e.deptno,d.dname from emp e left join dept d on e.deptno = d.deptno").print();

        //TODO 6.右外连接       左表:OnCreateAndWrite    右表:OnReadAndWrite
        //tableEnv.executeSql("select e.empno,e.ename,e.deptno,d.dname from emp e right join dept d on e.deptno = d.deptno").print();

        //TODO 7.全外连接       左表:OnReadAndWrite    右表:OnReadAndWrite
        //tableEnv.executeSql("select e.empno,e.ename,e.deptno,d.dname from emp e full join dept d on e.deptno = d.deptno").print();


        //TODO 8.将左外连接的结果 写到kafka主题
        //8.1 创建动态表和kafka主题关联
        
        //tableEnv          .from("")
        
        tableEnv.executeSql("CREATE TABLE kafka_emp (\n" +
            "  empno BIGINT,\n" +
            "  ename STRING,\n" +
            "  deptno BIGINT,\n" +
            "  dname STRING,\n" +
            " PRIMARY KEY (empno) NOT ENFORCED" +
            ") WITH (\n" +
            "  'connector' = 'upsert-kafka',\n" +
            "  'topic' = 'first',\n" +
            "  'properties.bootstrap.servers' = 'hadoop102:9092',\n" +
            "  'key.format' = 'json',\n" +
            "  'value.format' = 'json'\n" +
            ")");

        //8.2 向表中添加数据
        tableEnv.executeSql("insert into kafka_emp select e.empno,e.ename,e.deptno,d.dname from emp e left join dept d on e.deptno = d.deptno ");
    }
}
