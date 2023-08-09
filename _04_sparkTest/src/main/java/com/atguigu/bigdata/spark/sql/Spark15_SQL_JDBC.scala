package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql._


object Spark15_SQL_JDBC {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        val df = spark.read.format("jdbc")
            .option("url", "jdbc:mysql://hadoop102:3306/spark-sql")
            .option("driver", "com.mysql.jdbc.Driver")
            .option("user", "root")
            .option("password", "root")
            .option("dbtable", "user")
            .load()

        df.write.format("jdbc")
            .option("url", "jdbc:mysql://hadoop102:3306/spark-sql")
            .option("driver", "com.mysql.jdbc.Driver")
            .option("user", "root")
            .option("password", "root")
            .option("dbtable", "user4")
            //.mode("append")
            .save()

        spark.stop()

    }

}
