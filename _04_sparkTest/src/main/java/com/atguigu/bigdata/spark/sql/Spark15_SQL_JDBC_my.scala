package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql._


object Spark15_SQL_JDBC_my {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        val df = spark.read.format("jdbc")
            .option("url", "jdbc:mysql://hadoop102:3306/mysql") // ask
                // my:  2021/12/28 20:15  xception in thread "main" com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Unknown database 'girls'
                // 老师:
                // 下一次:

            .option("driver", "com.mysql.jdbc.Driver")
            .option("user", "root")
            .option("password", "root")
             .option("dbtable", "user")
            .load()

        df.write.format("jdbc")
            .option("url", "jdbc:mysql://hadoop102:3306/spark")
            .option("driver", "com.mysql.jdbc.Driver")
            .option("user", "root")
            .option("password", "root")
            .option("dbtable", "user577")
            //.mode("append")
            .save()

        spark.stop()



    }

}
