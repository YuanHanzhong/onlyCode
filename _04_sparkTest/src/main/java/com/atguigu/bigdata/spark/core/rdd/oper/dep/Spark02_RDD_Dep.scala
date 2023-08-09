package com.atguigu.bigdata.spark.core.rdd.oper.dep

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_Dep {

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
        println(fileRDD.dependencies)
        println("****************************************")
        val wordRDD: RDD[String] = fileRDD.flatMap(_.split(" "))
        println(wordRDD.dependencies)
        println("****************************************")
        val wordToOneRDD: RDD[(String, Int)] = wordRDD.map((_, 1))
        println(wordToOneRDD.dependencies)
        println("****************************************")
        val wordToCountRDD: RDD[(String, Int)] = wordToOneRDD.reduceByKey(_ + _)
        println(wordToCountRDD.dependencies)
        println("****************************************")
        wordToCountRDD.collect().foreach(println)

        sc.stop()

    }
}
