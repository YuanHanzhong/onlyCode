package com.atguigu.bigdata.spark.core.rdd.oper.dep

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Dep {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("WordCount")
        val sc = new SparkContext(conf)

        // 缺什么， 补什么？
        val fileRDD: RDD[String] = sc.textFile("_04_sparkTest/data/word.txt")
        println(fileRDD.toDebugString)
        println("****************************************")
        val wordRDD: RDD[String] = fileRDD.flatMap(_.split(" "))
        println(wordRDD.toDebugString)
        println("****************************************")
        val wordToOneRDD: RDD[(String, Int)] = wordRDD.map((_, 1))
        println(wordToOneRDD.toDebugString)
        println("****************************************")
        val wordToCountRDD: RDD[(String, Int)] = wordToOneRDD.reduceByKey(_ + _)
        println(wordToCountRDD.toDebugString)
        println("****************************************")
        wordToCountRDD.collect().foreach(println)

        sc.stop()

    }
}
