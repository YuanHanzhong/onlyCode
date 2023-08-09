package com.atguigu.bigdata.spark.core.wc

import org.apache.spark.{SparkConf, SparkContext}

object Spark07_WordCount {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        // Scala => Java => JVM => 内存（16G）
        // local => （一个线程）
        // local[2] => （两个线程）
        // local[16] => （16个线程）
        // local[*] => （当前环境中最大线程）
        val conf = new SparkConf().setMaster("local[*]").setAppName("WordCount")
        val sc = new SparkContext(conf)

        // 缺什么， 补什么？
        sc.textFile("_04_sparkTest/data/word.txt")
            .flatMap(_.split(" "))
            .map((_, 1))
            .reduceByKey(_+_)
            .collect()
            .foreach(println)

        sc.stop()

    }
}
