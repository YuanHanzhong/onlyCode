package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_Env {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境
        // SparkSQL的底层就是Spark Core,所以环境对象会进行封装
        //val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        //val sc = new SparkContext(conf)
        // 构建器模式
        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            //.config("spark.some.config.option", "some-value")
            //.config("spark.some.config.option", "some-value")
            .getOrCreate()

        val df: DataFrame = spark.read.json("data/user.json")
        //df.show()

        df.createTempView("user")

        spark.sql("select avg(age) from user").show

        spark.stop()

    }
}
