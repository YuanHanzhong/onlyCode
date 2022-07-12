package com.atguigu.gmall.realtime.common;

/**
 * Author: Felix
 * Date: 2022/5/18
 * Desc: 实时数仓系统常量类
 */
public class GmallConfig_m {
    public static final String PHOENIX_SCHEMA = "gmall2022_REALTIME";
    public static final String PHOENIX_DRIVER = "org.apache.phoenix.jdbc.PhoenixDriver";
    public static final String PHOENIX_URL = "jdbc:phoenix:hadoop102,hadoop103,hadoop104:2181";
    public static final String CLICKHOUSE_DRIVER = "ru.yandex.clickhouse.ClickHouseDriver";
    public static final String CLICKHOUSE_URL = "jdbc:clickhouse://hadoop102:8123/default";
    public static final String CHECK_POINT_STORAGE = "hdfs://hadoop102:8020/gmall2022/CheckPoint";
}
