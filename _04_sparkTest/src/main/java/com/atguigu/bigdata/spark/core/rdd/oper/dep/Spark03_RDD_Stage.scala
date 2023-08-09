package com.atguigu.bigdata.spark.core.rdd.oper.dep

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark03_RDD_Stage {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("WordCount")
        val sc = new SparkContext(conf)

        // 缺什么， 补什么？
        val fileRDD: RDD[String] = sc.makeRDD(
            List(
                "Hello World", "Hello Scala"
            )
        )
        val wordRDD: RDD[String] = fileRDD.flatMap(_.split(" "))
        val wordToOneRDD: RDD[(String, Int)] = wordRDD.map((_, 1))
        val wordToCountRDD: RDD[(String, Int)] = wordToOneRDD.reduceByKey(_ + _)
        wordToCountRDD.collect().foreach(println)

        sc.stop()

    }
}
