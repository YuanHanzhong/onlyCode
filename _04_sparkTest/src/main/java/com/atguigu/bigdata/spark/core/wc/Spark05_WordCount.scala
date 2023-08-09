package com.atguigu.bigdata.spark.core.wc

import org.apache.spark.{SparkConf, SparkContext}

object Spark05_WordCount {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)

        // 缺什么， 补什么？
        sc.textFile("_04_sparkTest/data/word.txt")
            .flatMap(_.split(" "))
            .groupBy(word => word)
            .mapValues(_.size)
            .collect()
            .foreach(println)

        sc.stop()

    }
}
