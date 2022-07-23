package com.atguigu.gmall.realtime.util;

import com.atguigu.gmall.realtime.bean.TransientSink;
import com.atguigu.gmall.realtime.common.GmallConfig;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Author: Felix
 * Date: 2022/7/10
 * Desc: 操作ClickHouse的工具类
 */
public class MyClickHouseUtil {
    public static <T>SinkFunction<T> getJdbcSinkFunction(String sql){
        SinkFunction<T> sinkFunction = JdbcSink.<T>sink(
            //insert into dws_traffic_source_keyword_page_view_window values(?,?,?,?,?,?)
            sql,
            new JdbcStatementBuilder<T>() {
                @Override
                public void accept(PreparedStatement ps, T obj) throws SQLException {
                    //给？号占位符赋值：获取流中对象的属性值，将属性值赋给对应的？号占位符
                    //获取当前流中对象类型以及类中所有的属性
                    Field[] fieldArr = obj.getClass().getDeclaredFields();
                    //遍历类中所有的属性对象
                    int skipNum = 0;
                    for (int i = 0; i < fieldArr.length; i++) {
                        Field field = fieldArr[i];
                        //判断当前属性是否需要保存到CK中
                        TransientSink transientSink = field.getAnnotation(TransientSink.class);
                        if(transientSink != null){
                            skipNum++;
                            continue;
                        }
                        //设置访问权限
                        field.setAccessible(true);
                        try {
                            Object fieldValue = field.get(obj);
                            //赋值
                            ps.setObject(i + 1 - skipNum ,fieldValue);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            },
            //new JdbcExecutionOptions.Builder()
            JdbcExecutionOptions.builder()
                .withBatchSize(5)
                .withBatchIntervalMs(5)
                .build(),
            new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                .withDriverName(GmallConfig.CLICKHOUSE_DRIVER)
                .withUrl(GmallConfig.CLICKHOUSE_URL)
                .build()
        );
        return sinkFunction;
    }
}
