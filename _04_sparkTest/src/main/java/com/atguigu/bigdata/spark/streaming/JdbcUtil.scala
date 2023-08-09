package com.atguigu.bigdata.spark.streaming

import com.alibaba.druid.pool.DruidDataSourceFactory

import java.sql.Connection
import java.util.Properties
import javax.sql.DataSource

object JdbcUtil {
    //初始化连接池
    var dataSource: DataSource = init()

    //初始化连接池方法
    def init(): DataSource = {
        println("JdbcUtil init...")
        val properties = new Properties()

        properties.setProperty("driverClassName", "com.mysql.jdbc.Driver")
        properties.setProperty("url", "jdbc:mysql://hadoop102:3306/spark-streaming210906")
        properties.setProperty("username", "root")
        properties.setProperty("password", "root")
        properties.setProperty("maxActive", "20")
        DruidDataSourceFactory.createDataSource(properties)
    }

    //获取MySQL连接
    def getConnection: Connection = {
        dataSource.getConnection
    }
}
