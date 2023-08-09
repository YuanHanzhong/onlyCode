package com.atguigu.bigdata.spark.sql

//package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql._


object Spark16_SQL_Hive_my {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
             .enableHiveSupport() // 启用Hive的支持
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        // TODO Spark中默认包含的Hive的内容的，可以直接使用。这种方式称之为：内置Hive
        //      工作中，一般是需要外部的Hive进行连接
        spark.sql("show tables").show

        spark.stop()

    }

}
