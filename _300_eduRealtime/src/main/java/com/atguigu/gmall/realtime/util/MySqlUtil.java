package com.atguigu.gmall.realtime.util;

/**
 * Author: Felix
 * Date: 2022/7/8
 * Desc: 获取维度表相关的建表语句
 */
public class MySqlUtil {
    public static String getBaseDicLookUpDDL(){
        return "CREATE TABLE base_dic (\n" +
            "  dic_code string,\n" +
            "  dic_name string,\n" +
            "  parent_code string,\n" +
            "  create_time timestamp,\n" +
            "  operate_time timestamp,\n" +
            "  PRIMARY KEY (dic_code) NOT ENFORCED\n" +
            ") " + getJdbcConnectorDDL("base_dic");
    }

    public static String getJdbcConnectorDDL(String tableName){
        return "WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://hadoop102:3306/education',\n" +
                "   'table-name' = '"+tableName+"',\n" +
                "   'username' = 'root',\n" +
                "   'password' = 'root',\n" +
                "   'driver' = 'com.mysql.cj.jdbc.Driver',\n" +
                "   'lookup.cache.max-rows' = '200',\n" +
                "   'lookup.cache.ttl' = '1 hour'\n" +
                ")";
    }
}
