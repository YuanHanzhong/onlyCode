package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql.{DataFrame, SparkSession}

object Spark07_SQL_Test {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        // RDD的实现方式
//        spark.sparkContext.makeRDD(
//            List(
//                "zhangsan", "lisi", "wangwu"
//            )
//        ).map(
//            name => {
//                "Name:" + name
//            }
//        ).collect().foreach(println)

        // SQL的方式
        val df: DataFrame = spark.read.json("data/user.json")
        df.createTempView("user")
        // zhangsan => Name:zhangsan
        // TODO SQL不是万能的，如果实现不了业务功能，就需要自己定义方法来实现 : UDF
        // UDF : 自己定义一个函数在SQL中使用
        spark.udf.register("prefixName", (name:String) => {
            "Name:" + name
        })
        spark.sql("select prefixName(name) from user").show

        spark.stop()

    }
}
