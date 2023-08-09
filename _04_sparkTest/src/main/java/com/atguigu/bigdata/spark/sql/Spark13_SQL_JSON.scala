package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql._


object Spark13_SQL_JSON {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        // json格式的文件有格式上的要求：整个文件的格式符合JSON格式
        // SparkSQL基于SparkCore
        // SparkCore读取文件基于Hadoop
        // Hadoop读取文件按行读取
        // SparkSQL读取JSON文件，只要保证一行的数据符合JSON格式即可
        val df = spark.read.json("data/user.json")
        df.show

        spark.stop()

    }

}
