package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{Encoder, _}


object Spark11_SQL_SaveOrLoad {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()
        import spark.implicits._

        // TODO load方法表示通用的读取方法
        //      sparksql默认读取的文件格式为：Parquet（列式存储格式）
        // TODO
        //  user.json is not a Parquet file. expected magic number at tail [80, 65, 82, 49] but found [32, 57, 48, 125]
        //val df: DataFrame = spark.read.load("data/users.parquet")
        //val df: DataFrame = spark.read.format("json").load("data/user.json")
        val df: DataFrame = spark.read.json("data/user.json")
        df.show

        spark.stop()

    }

}
