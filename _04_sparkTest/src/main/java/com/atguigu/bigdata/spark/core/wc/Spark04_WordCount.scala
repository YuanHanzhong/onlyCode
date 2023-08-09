package com.atguigu.bigdata.spark.core.wc

import org.apache.spark.{SparkConf, SparkContext}

object Spark04_WordCount {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)

        println(System.getProperty("user.dir"))
        val lines = sc.textFile("data/word.txt")
        val words = lines.flatMap(_.split(" "))
        val wordGroup = words.groupBy(word => word)
        val wordCount = wordGroup.mapValues(_.size)
        wordCount.collect().foreach(println)

        sc.stop()

    }
}
