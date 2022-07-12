package com.atguigu.gmall.realtime.app.dwd.db;

import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/*
 * Author: Felix
 * Date: 2022/5/27
 * Desc: 交易域下单事务事实表
 * 需要启动的进程
 *      zk、kafka、maxwell、DwdTradeOrderPreProcess、DwdTradeOrderDetail
 * 执行流程
 *      运行模拟生成业务数据的jar
 *      生成业务数据到业务数据库对应的表中
 *      binlog会记录业务数据库表的变化
 *      maxwell从binlog中读取变化的数据，并封装为json格式字符串，发送到kafka的topic_db主题中
 *      DwdTradeOrderPreProcess从topic_db主题中读取订单明细、订单、订单明细活动、订单明细优惠券数据并和字典表进行关联，
 *          得到预处理宽表,并写到kafka的dwd_trade_order_pre_process主题中
 *      DwdTradeOrderDetail从dwd_trade_order_pre_process主题读取数据，并将下单数据过滤出来
 *      将过滤出来的下单数据发送到kafka的dwd_trade_order_detail主题中
 */
public class DwdTradeOrderDetail {
    public static void main(String[] args) {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //1.3 指定表执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        /*
        //TODO 2.检查点相关的设置
        //2.1 开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        //2.2 设置检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.3 设置job取消后 检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //2.4 设置两个检查点之间最小时间间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);
        //2.5 设置重启策略
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30),Time.seconds(3)));
        //2.6 设置状态后端
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/xxxx");
        //2.7 设置操作hadoop的用户
        System.setProperty("HADOOP_USER_NAME","atguigu");*/

        //TODO 3.从kafka订单预处理主题中读取数据创建动态表
        tableEnv.executeSql("create table dwd_trade_order_pre_process(\n" +
            "id string,\n" +
            "order_id string,\n" +
            "user_id string,\n" +
            "order_status string,\n" +
            "sku_id string,\n" +
            "sku_name string,\n" +
            "province_id string,\n" +
            "activity_id string,\n" +
            "activity_rule_id string,\n" +
            "coupon_id string,\n" +
            "date_id string,\n" +
            "create_time string,\n" +
            "operate_date_id string,\n" +
            "operate_time string,\n" +
            "source_id string,\n" +
            "source_type string,\n" +
            "source_type_name string,\n" +
            "sku_num string,\n" +
            "split_original_amount string,\n" +
            "split_activity_amount string,\n" +
            "split_coupon_amount string,\n" +
            "split_total_amount string,\n" +
            "`type` string,\n" +
            "`old` map<string,string>,\n" +
            "od_ts string,\n" +
            "oi_ts string,\n" +
            "row_op_ts timestamp_ltz(3)\n" +
            ")" + MyKafkaUtil.getKafkaDDL(
            "dwd_trade_order_pre_process", "dwd_trade_order_detail"));


        //TODO 4.从动态表中将下单行为过滤出来  得到的是下单动态表
        Table filteredTable = tableEnv.sqlQuery("" +
            "select " +
            "id,\n" +
            "order_id,\n" +
            "user_id,\n" +
            "sku_id,\n" +
            "sku_name,\n" +
            "province_id,\n" +
            "activity_id,\n" +
            "activity_rule_id,\n" +
            "coupon_id,\n" +
            "date_id,\n" +
            "create_time,\n" +
            "source_id,\n" +
            "source_type source_type_code,\n" +
            "source_type_name,\n" +
            "sku_num,\n" +
            "split_original_amount,\n" +
            "split_activity_amount,\n" +
            "split_coupon_amount,\n" +
            "split_total_amount,\n" +
            "od_ts ts,\n" +
            "row_op_ts\n" +
            "from dwd_trade_order_pre_process " +
            "where `type`='insert'");
        tableEnv.createTemporaryView("filtered_table", filteredTable);


        //TODO 5.创建动态表  和要写到的kafka主题对应
        tableEnv.executeSql("" +
            "create table dwd_trade_order_detail(\n" +
            "id string,\n" +
            "order_id string,\n" +
            "user_id string,\n" +
            "sku_id string,\n" +
            "sku_name string,\n" +
            "province_id string,\n" +
            "activity_id string,\n" +
            "activity_rule_id string,\n" +
            "coupon_id string,\n" +
            "date_id string,\n" +
            "create_time string,\n" +
            "source_id string,\n" +
            "source_type_code string,\n" +
            "source_type_name string,\n" +
            "sku_num string,\n" +
            "split_original_amount string,\n" +
            "split_activity_amount string,\n" +
            "split_coupon_amount string,\n" +
            "split_total_amount string,\n" +
            "ts string,\n" +
            "row_op_ts timestamp_ltz(3),\n" +
            "primary key(id) not enforced\n" +
            ")" + MyKafkaUtil.getUpsertKafkaDDL("dwd_trade_order_detail"));

        //TODO 6.将下单数据写到对应的kafka主题
        tableEnv.executeSql("insert into dwd_trade_order_detail select * from filtered_table");

    }
}
