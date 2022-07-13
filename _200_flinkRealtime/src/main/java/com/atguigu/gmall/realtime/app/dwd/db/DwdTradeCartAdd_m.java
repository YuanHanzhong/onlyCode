package com.atguigu.gmall.realtime.app.dwd.db;

import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import com.atguigu.gmall.realtime.util.MySqlUtil;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.Duration;

/*
 * Desc: 交易域加购事务事实表
 * 需要启动的进程
 *      zk、kafka、[hdfs]、maxwell、DwdTradeCartAdd
 * 执行流程
 *      业务系统产生加购业务数据
 *      加购业务数据会保存到业务数据库MySQL中
 *      binlog会记录业务数据库表的变化，并将变化数据封装为json发送到kafka的topic_db
 *      DwdTradeCartAdd从topic_db主题中获取所有业务数据创建动态表
 *      过滤加购得到加购动态表
 *      从mysql中查询字段表数据创建字典动态表
 *      将加购动态表和字典动态表进行关联
 *      创建动态表映射要写入的kafka主题
 *      将关联的结果写到kafka主题
 */
public class DwdTradeCartAdd_m {
    public static void main(String[] args) {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //1.3 设置表执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        //1.4 设置关联的时候状态的过期时间
        tableEnv.getConfig().setIdleStateRetention(Duration.ofSeconds(5));

        //TODO 2.检查点相关设置(略)
        //TODO 3.从kafka的topic_db主题中读取业务数据，创建动态表
        // 2022/7/8 10:12 NOTE 加上飘号比较保险
        // 2022/7/8 10:24 NOTE excutesql得到表, sqlquery得到流
        tableEnv.executeSql("CREATE TABLE topic_db (\n" +
            "  `database` string,\n" +
            "  `table` string,\n" +
            "  `type` string,\n" +
            "  `ts` STRING,\n" +
            "  `proc_time` as proctime(),\n" +
            "  `data` MAP<string, string>,\n" +
            "  `old` MAP<string, string>\n" +
                              // 2022/7/8 10:20 NOTE 抽取工具
            " )" + MyKafkaUtil.getKafkaDDL("topic_db","dwd_trade_cart_add_group"));

        //tableEnv.executeSql("select * from topic_db").print();


        //TODO 4.从上面创建的动态表中过滤出加购数据--加购动态表
        Table cartAdd = tableEnv.sqlQuery("select \n" +
            "    data['id'] id,\n" +
         //2022/7/8 10:27 NOTE data本身是map, 获取map某字段就是用[]的方式
            "    data['user_id'] user_id,\n" + // 2022/7/8 11:09 NOTE 后边的是别名
            "    data['sku_id'] sku_id,\n" +
            "    data['source_type'] source_type,\n" +
            "    data['source_id'] source_id,\n" +
            "    if(`type`='insert',\n" +
            "    data['sku_num'],\n" +
            " cast((cast(data['sku_num'] as int) - cast(`old`['sku_num'] as int)) as string)) sku_num,\n" +
            " ts,proc_time\n" +
            "from \n" +
            "    topic_db\n" +
            "where\n" +
            "    `table` = 'cart_info'\n" +
            " and\n" +
            
            " (`type` = 'insert' " +
            "or (`type` = 'update' and `old`['sku_num'] is not null and cast(data['sku_num'] as int) > cast(`old`['sku_num'] as int)))");
    
        // 2022/7/8 11:08 NOTE 方法1
        tableEnv.createTemporaryView("cart_add",cartAdd);
        // 2022/7/8 11:08 NOTE 方法2
        //tableEnv.executeSql("select * from" + cart_add).print();

        //TODO 5.从数据库中读取字典表数据创建动态表
        tableEnv.executeSql(MySqlUtil.getBaseDicDDL());  // 2022/7/8 11:26 NOTE excu之后就提交了, 所以只能有一个
        //tableEnv.executeSql("select * from base_dic").print();


        //TODO 6.关联两张表  （加购动态表 + 字典表）
        Table resTable = tableEnv.sqlQuery("select\n" +
            " id,user_id,sku_id,source_id,source_type,dic_name source_type_name,sku_num,ts\n" +
            "from\n" +
            " cart_add cadd\n" +
            " join\n" +
            " base_dic FOR SYSTEM_TIME AS OF cadd.proc_time AS  dic\n" +
            " on\n" +
            " cadd.source_type = dic.dic_code");
        tableEnv.createTemporaryView("res_table",resTable);

        //tableEnv.executeSql("select * from res_table").print();
        
        
        //TODO 7.创建动态表  关联kafka的dwd_trade_cart_add
        tableEnv.executeSql("CREATE TABLE dwd_trade_cart_add (\n" +
            "  id string,\n" +
            "  user_id string,\n" +
            "  sku_id string,\n" +
            "  source_id string,\n" +
            "  source_type string,\n" +
            "  source_type_name string,\n" +
            "  sku_num string,\n" +
            "  ts string,\n" +
            "  PRIMARY KEY (id) NOT ENFORCED\n" +
            ") " +MyKafkaUtil.getUpsertKafkaDDL("dwd_trade_cart_add"));
    
        //TODO 8.将关联的数据写到 kafka输出主题对应的动态表中
        tableEnv.executeSql("insert into dwd_trade_cart_add select * from res_table");

    }
}
