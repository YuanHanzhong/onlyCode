package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql._


object Spark14_SQL_CSV_my {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        spark.read.format("csv")
            .option("sep", ";")
            .option("inferSchema", "true")
            .option("header", "true")
            .load("data/people.csv").show

        spark.stop()

    }

}
