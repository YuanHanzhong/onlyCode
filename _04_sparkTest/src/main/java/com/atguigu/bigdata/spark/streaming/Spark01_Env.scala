package com.atguigu.bigdata.spark.streaming

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SparkJdbc {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        // JDBC Connection(url, user, password)
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)

        val rdd: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4), 2)
        rdd.foreachPartition(
            iter => {
                val conn = JdbcUtil.getConnection
                iter.foreach(println)
                conn.close()
            }
        )


        sc.stop()

    }
}
