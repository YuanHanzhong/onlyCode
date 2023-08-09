package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql.{DataFrame, SparkSession}

object Spark03_SQL_DataFrame_DSL {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")

            .getOrCreate()

        // 读取DataFrame
        val df: DataFrame = spark.read.json("data/user.json")
        // 使用DSL的方式访问DataFrame
        df.select("*").show


        spark.stop()

    }
}
