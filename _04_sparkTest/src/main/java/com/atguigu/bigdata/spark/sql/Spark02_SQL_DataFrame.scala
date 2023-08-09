package com.atguigu.bigdata.spark.sql

//package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql.{DataFrame, SparkSession}

object Spark02_SQL_DataFrame {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")

            .getOrCreate()

        // 读取DataFrame
        val df: DataFrame = spark.read.json("data/user.json")
        // 使用SQL的方式访问DataFrame
        df.createTempView("user")
        //df.createOrReplaceTempView("user")
        //df.createGlobalTempView("user")
        //df.createOrReplaceGlobalTempView()
        spark.sql("select avg(age) from user").show

        spark.stop()

    }
}
