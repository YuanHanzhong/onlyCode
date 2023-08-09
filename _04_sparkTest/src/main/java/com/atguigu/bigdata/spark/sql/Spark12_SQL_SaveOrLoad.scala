package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql._


object Spark12_SQL_SaveOrLoad {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        val df = spark.read.json("data/user.json")
        // TODO Sparksql默认保存的文件格式为parquet,如果不想保存为parquet文件，可以修改格式
        df.write.format("json").mode("append").save("output")

        spark.stop()

    }

}
