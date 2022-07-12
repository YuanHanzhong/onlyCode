package com.atguigu.gmall.realtime.app.dwd.db;

import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import com.atguigu.gmall.realtime.util.MySqlUtil;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.Duration;

/**
 * Desc: 交易域支付成功事务事实表
 * 需要启动的进程
 *      zk、kafka、maxwell、DwdTradeOrderPreProcess、DwdTradeOrderDetail、DwdTradePayDetailSuc
 */
public class DwdTradePayDetailSuc_m_0709 {
    public static void main(String[] args) {
        //TODO 1.基本环境准备
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        tableEnv.getConfig().setIdleStateRetention(Duration.ofSeconds(60*15));

        //TODO 2.检查点相关设置(略)

        //TODO 3.从kafka的下单主题dwd_trade_order_detail主题中读取下单数据 创建动态表
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
            "row_op_ts timestamp_ltz(3)\n" +
            ")" + MyKafkaUtil.getKafkaDDL("dwd_trade_order_detail", "dwd_trade_pay_detail_suc"));

        //TODO 4.从kakfa的业务主题topic_db中读取数据创建动态表
        tableEnv.executeSql("create table topic_db(" +
            "`database` String,\n" +
            "`table` String,\n" +
            "`type` String,\n" +
            "`data` map<String, String>,\n" +
            "`old` map<String, String>,\n" +
            "`proc_time` as PROCTIME(),\n" +
            "`ts` string\n" +
            ")" + MyKafkaUtil.getKafkaDDL("topic_db", "dwd_trade_pay_detail_suc"));

        //TODO 5.从业务数据中将支付数据过滤出来
        Table paymentInfo = tableEnv.sqlQuery("select\n" +
                "data['user_id'] user_id,\n" +
                "data['order_id'] order_id,\n" +
                "data['payment_type'] payment_type,\n" +
                "data['callback_time'] callback_time,\n" +
                "`proc_time`,\n" +
                "ts\n" +
                "from topic_db\n" +
                "where `table` = 'payment_info'\n"
//                +
//                "and `type` = 'update'\n" +
//                "and data['payment_status']='1602'" // 2022/7/9 10:21 NOTE 模拟的数据优点问题, 注释掉
        );
        tableEnv.createTemporaryView("payment_info", paymentInfo);

        //TODO 6.从MySQL的字典表中读取数据
        tableEnv.executeSql(MySqlUtil.getBaseDicDDL());

        //TODO 7.关联3张表
        Table resultTable = tableEnv.sqlQuery("" +
            "select\n" +
            "od.id order_detail_id,\n" +
            "od.order_id,\n" +
            "od.user_id,\n" +
            "od.sku_id,\n" +
            "od.sku_name,\n" +
            "od.province_id,\n" +
            "od.activity_id,\n" +
            "od.activity_rule_id,\n" +
            "od.coupon_id,\n" +
            "pi.payment_type payment_type_code,\n" +
            "dic.dic_name payment_type_name,\n" +
            "pi.callback_time,\n" +
            "od.source_id,\n" +
            "od.source_type_code,\n" +
            "od.source_type_name,\n" +
            "od.sku_num,\n" +
            "od.split_original_amount,\n" +
            "od.split_activity_amount,\n" +
            "od.split_coupon_amount,\n" +
            "od.split_total_amount split_payment_amount,\n" +
            "pi.ts,\n" +
            "od.row_op_ts row_op_ts\n" +
            "from payment_info pi\n" +
            "join dwd_trade_order_detail od\n" +
            "on pi.order_id = od.order_id\n" +
            "join `base_dic` for system_time as of pi.proc_time as dic\n" +
            "on pi.payment_type = dic.dic_code");
        tableEnv.createTemporaryView("result_table", resultTable);

        //TODO 8.创建动态表 和写到kafka的主题对应
        tableEnv.executeSql("create table dwd_trade_pay_detail_suc(\n" +
            "order_detail_id string,\n" +
            "order_id string,\n" +
            "user_id string,\n" +
            "sku_id string,\n" +
            "sku_name string,\n" +
            "province_id string,\n" +
            "activity_id string,\n" +
            "activity_rule_id string,\n" +
            "coupon_id string,\n" +
            "payment_type_code string,\n" +
            "payment_type_name string,\n" +
            "callback_time string,\n" +
            "source_id string,\n" +
            "source_type_code string,\n" +
            "source_type_name string,\n" +
            "sku_num string,\n" +
            "split_original_amount string,\n" +
            "split_activity_amount string,\n" +
            "split_coupon_amount string,\n" +
            "split_payment_amount string,\n" +
            "ts string,\n" +
            "row_op_ts timestamp_ltz(3),\n" +
            "primary key(order_detail_id) not enforced\n" +
            ")" + MyKafkaUtil.getUpsertKafkaDDL("dwd_trade_pay_detail_suc"));

        //TODO 9.将关联的结果写到kafka主题中
        tableEnv.executeSql("" +
            "insert into dwd_trade_pay_detail_suc select * from result_table");

    }
}
