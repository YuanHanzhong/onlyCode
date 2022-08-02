package com.atguigu.gmall.realtime.app.dwd;

import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import com.atguigu.gmall.realtime.util.MySqlUtil;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * Author: Felix
 * Date: 2022/7/8
 * Desc: 交易域-加购事务事实表
 * -从kafka的ODS_BASE_EDU_DB主题中读取业务数据，创建动态表
 * 	-从中过滤出加购数据
 * 		过滤条件
 * 			table：cart_info
 * 			type: insert or (update old[sku_num] is not null data[sku_num] > old[sku_num])
 * 		注意：sqlQuery查询得到的Table对象，需要将其注册到TableEnv中
 * 			tableEnv.createTemporaryView
 * 			"select * from "+ 表名
 * 	-从MySQL字典表中读取数据  创建动态表
 * 	-使用lookup join 将字典维度退化到加购表中
 * 	-创建动态表和要写入的kafka主题建立起映射关系
 * 	-将数据写到kafka主题dwd_trade_cart_add中
 */
public class DwdTradeCartAdd {
    public static void main(String[] args) {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //1.3 指定表执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //TODO 2.检查点相关设置(略)

        //TODO 3.从kafka的ODS_BASE_EDU_DB主题中读取数据  创建业务数据动态表
        String topic = "ODS_BASE_EDU_DB";
        String groupId = "dwd_trade_cart_add_group";
        tableEnv.executeSql("create table ODS_BASE_EDU_DB(\n" +
            " `database` string,\n" +
            " `table` string,\n" +
            " `type` string,\n" +
            " `ts` string,\n" +
            " `old` map<string,string>,\n" +
            " `data` map<string,string>,\n" +
            " `proc_time` as proctime()\n" +
            ") " + MyKafkaUtil.getKafkaDDL(topic,groupId));
        //tableEnv.executeSql("select * from ODS_BASE_EDU_DB").print();

        //TODO 4.从上面的业务数据动态表中过滤出加购操作  得到加购动态表
        Table cartAddTable = tableEnv.sqlQuery("select \n" +
            "    data['id'] id,\n" +
            "    data['user_id'] user_id,\n" +
            "    data['sku_id'] sku_id,\n" +
            "    data['source_type'] source_type,\n" +
            "    if(type='insert',data['sku_num'],cast((CAST(data['sku_num'] AS INT) " +
            "       - CAST(`old`['sku_num'] AS INT)) as string)) sku_num,\n" +
            "    ts,\n" +
            "    proc_time\n" +
            "from \n" +
            "    ODS_BASE_EDU_DB\n" +
            "where \n" +
            "  `table`='cart_info'\n" +
            "and\n" +
            "    (type='insert' or (type='update'   and CAST(`old`['sku_num'] AS INT) is not null " +
            " and (CAST(data['sku_num'] AS INT) > CAST(`old`['sku_num'] AS INT))))");

        //tableEnv.executeSql("select * from " + cartAddTable);

        tableEnv.createTemporaryView("cart_add",cartAddTable);
        //tableEnv.executeSql("select * from cart_add").print();
        //TODO 5.从MySQL字典表中读取数据，创建字典动态表   base_dic
        tableEnv.executeSql(MySqlUtil.getBaseDicLookUpDDL());

        //tableEnv.executeSql("select * from base_dic").print();

        //TODO 6.使用lookup join将字典维度数据退化到加购事实表中
        Table joinedTable = tableEnv.sqlQuery("SELECT\n" +
            " id,\n" +
            " user_id,\n" +
            " sku_id,\n" +
            " sku_num,\n" +
            " source_type,\n" +
            " dic_name source_type_name,\n" +
            " ts\n" +
            "FROM \n" +
            " cart_add c JOIN base_dic FOR SYSTEM_TIME AS OF c.proc_time as d ON c.source_type = d.dic_code");
        tableEnv.createTemporaryView("joined_table",joinedTable);

        //TODO 7.创建一张动态表  和要写入的kafka主题进行映射
        tableEnv.executeSql("CREATE TABLE dwd_trade_cart_add (\n" +
            "  id string,\n" +
            "  uesr_id string,\n" +
            "  sku_id string,\n" +
            "  sku_num string,\n" +
            "  source_type string,\n" +
            "  source_type_name string,\n" +
            "  ts string,\n" +
            "    PRIMARY KEY (id) NOT ENFORCED\n" +
            ") " + MyKafkaUtil.getUpsertKafkaDDL("dwd_trade_cart_add"));

        //TODO 8.将退化后的数据写到kafka主题中
        tableEnv.executeSql("insert into dwd_trade_cart_add select * from joined_table");
    }
}
