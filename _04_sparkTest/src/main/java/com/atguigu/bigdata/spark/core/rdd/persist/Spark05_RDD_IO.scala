package com.atguigu.bigdata.spark.core.rdd.persist

import org.apache.spark.{SparkConf, SparkContext}

object Spark05_RDD_IO {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)

        val rdd = sc.makeRDD(
            List(
                ("a", 1), ("b", 2), ("c", 3)
            )
        )

        rdd.saveAsTextFile("output")
        rdd.saveAsObjectFile("output1")
        rdd.saveAsSequenceFile("output2") // KV类型才能使用


        sc.stop()

    }
}
