package com.atguigu.gmall.realtime.app.dwd.db;

import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * Author: Felix
 * Date: 2022/5/27
 * Desc: 工具域优惠劵使用(下单)事务事实表
 * 启动程序:
 */
public class DwdToolCouponOrder_m_0709 {
    public static void main(String[] args) throws Exception {

        // TODO 1. 环境准备
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // TODO 2. 检查点相关设置(略)

        // TODO 3. 从 Kafka 读取 topic_db 数据，封装为 Flink SQL 表
        tableEnv.executeSql(MyKafkaUtil.getTopicDbDDL("dwd_tool_coupon_order"));

        // TODO 4. 读取优惠券领用表数据，筛选满足条件的优惠券下单数据
        Table couponUseOrder = tableEnv.sqlQuery("select\n" +
            "data['id'] id,\n" +
            "data['coupon_id'] coupon_id,\n" +
            "data['user_id'] user_id,\n" +
            "data['order_id'] order_id,\n" +
            "date_format(data['using_time'],'yyyy-MM-dd') date_id,\n" +
            "data['using_time'] using_time,\n" +
            "ts\n" +
            "from topic_db\n" +
            "where `table` = 'coupon_use'\n" +
            //"and `type` = 'update'\n" +
            //"and data['coupon_status'] = '1402'\n" +
            //"and `old`['coupon_status'] = '1401'" +
                                                   "");

        tableEnv.createTemporaryView("result_table", couponUseOrder);
    
        // 2022/7/10 8:03 NOTE GOT 输出下结果
        tableEnv.executeSql("select * from result_table").print(); // 注释掉.print(); 后数据就能到kafka了
        tableEnv.sqlQuery("select * from result_table");//.print(); // 注释掉.print(); 后数据就能到kafka了
        // 2022/7/10 10:12 NOTE ASK sqlQurey, 只能输出一次,
    
    
        // TODO 5. 建立 Upsert-Kafka dwd_tool_coupon_order 表
        tableEnv.executeSql("create table dwd_tool_coupon_order(\n" +
            "id string,\n" +
            "coupon_id string,\n" +
            "user_id string,\n" +
            "order_id string,\n" +
            "date_id string,\n" +
            "order_time string,\n" +
            "ts string,\n" +
            "primary key(id) not enforced\n" +
            ")" + MyKafkaUtil.getUpsertKafkaDDL("dwd_tool_coupon_order"));

        // TODO 6. 将数据写入 Upsert-Kafka 表
        tableEnv.executeSql("" +
            "insert into dwd_tool_coupon_order select " +
            "id,\n" +
            "coupon_id,\n" +
            "user_id,\n" +
            "order_id,\n" +
            "date_id,\n" +
            "using_time order_time,\n" +
            "ts from result_table");

    }

}
