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

/*
// 2022/7/10 14:28 NOTE 抽取
 * Desc: 操作ClickHouse的工具类
 */
public class MyClickHouseUtil_m_0710 {
    //获取SinkFunction
    public static <T>SinkFunction<T> getJdbcSink(String sql){ // 2022/7/10 14:24 NOTE 泛型的声明
        SinkFunction<T> sinkFunction = JdbcSink.sink(
            sql,
            new JdbcStatementBuilder<T>() {
                @Override
                public void accept(PreparedStatement ps, T obj) throws SQLException {
                    //---------将流中对象的属性给问号占位符赋值   insert into 表 values(?,?,?,?,?,?)----------
                    //通过反射获取当前流中对象所属类中都有哪些属性
                    // 2022/7/10 14:33 NOTE 反射的应用, 通过反射可以拿到想要的一切
                    Field[] fieldArr = obj.getClass().getDeclaredFields();
                    //对获取到的属性进行遍历
                    int skipCount = 0;
                    for (int i = 0; i < fieldArr.length; i++) {
                        //获取一个个属性对象
                        Field field = fieldArr[i];

                        //判断当前属性是否需要保存到CK中
                        TransientSink transientSink = field.getAnnotation(TransientSink.class);
                        if(transientSink != null){ //说明有了TansientSink这个注解
                            skipCount ++;
                            continue; //不需要保存, 就直接进行下一轮
                        }

                        //设置私有属性的访问权限
                        field.setAccessible(true);
                        try {
                            //获取属性的值
                            Object fieldValue = field.get(obj);
                            //将属性的值赋值对应的问号占位符
                            ps.setObject(i + 1 - skipCount ,fieldValue);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            },
            new JdbcExecutionOptions.Builder()
                .withBatchSize(5)
                .withBatchIntervalMs(1000)
                .build(),

            new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                .withDriverName(GmallConfig.CLICKHOUSE_DRIVER)
                .withUrl(GmallConfig.CLICKHOUSE_URL)
                .build()
        );
        return sinkFunction;
    }

}
