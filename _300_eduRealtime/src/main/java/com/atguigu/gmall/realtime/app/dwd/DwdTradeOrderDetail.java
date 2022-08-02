package com.atguigu.gmall.realtime.app.dwd;

import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * Author: Felix
 * Date: 2022/7/9
 * Desc:交易域--下单事务事实表
 * 需要启动的进程
 *      zk、kafka、maxwell、DwdTradeOrderPreProcess、DwdTradeOrderDetail
 * 执行流程
 *      运行模拟生成业务数据的jar
 *      会模拟生成订单、订单明细、订单明细活动、订单明细优惠券等业务数据保存到业务数据库表中
 *      binlog会记录业务数据库表的变化
 *      maxwell从binlog中读取变化的数据，并将其封装为json字符串，发送给kafka的ODS_BASE_EDU_DB主题
 *      DwdTradeOrderPreProcess从ODS_BASE_EDU_DB主题中读取数据，并过滤出订单、订单明细、订单明细活动、订单明细优惠券
 *          数据，并和字段表进行关联,将关联的结果写到kafka的订单预处理表dwd_trade_order_pre_process中
 *      DwdTradeOrderDetail从订单预处理表dwd_trade_order_pre_process中读取数据
 *      过滤出type='insert'的操作，就是下单行为
 *      将下单数据写到kafka的dwd_trade_order_detail主题(下单事务事实表)
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

        //TODO 2.检查点相关设置(略)

        //TODO 3.从kafka的订单预处理主题中读取数据，创建动态表
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
            ")" + MyKafkaUtil.getKafkaDDL("dwd_trade_order_pre_process", "dwd_trade_order_detail"));


        //TODO 4.过滤出下单操作
        Table filteredTable = tableEnv.sqlQuery("select " +
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

        //TODO 5.创建动态表  和要写入的kafka的下单事实表主题进行映射
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

        //TODO 6.将下单数据写单kafka主题中
        tableEnv.executeSql("insert into dwd_trade_order_detail select * from filtered_table");

    }
}
