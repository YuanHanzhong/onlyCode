package com.atguigu.bigdata.spark.core.wc

import org.apache.spark.{SparkConf, SparkContext}

object Spark06_WordCount {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)

        // 缺什么， 补什么？
        sc.textFile("data/word.txt")
            .flatMap(
                line => {
                    val words = line.split(" ")
                    words.map((_, 1))
                }
            ) // (word, 1)
            .groupBy(_._1) // ( word, List( (word, 1), (word, 1) ...) )
//            .mapValues(
//                list => {
//                    list.map(_._2).sum
//                }
//            )
            .mapValues(_.map(_._2).sum)
            .collect()
            .foreach(println)

        sc.stop()

    }
}
