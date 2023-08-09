package com.atguigu.bigdata.spark.core.wc

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark08_WordCount {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("WordCount")
        val sc = new SparkContext(conf)

        // 缺什么， 补什么？
        val lineRDD: RDD[String] = sc.textFile("data/word.txt")
        val wordRDD: RDD[String] = lineRDD.flatMap(_.split(" "))
        val wordToOneRDD: RDD[(String, Int)] = wordRDD.map(
            word => {
                println("************")
                (word, 1)
            })
        val wordToCountRDD: RDD[(String, Int)]  = wordToOneRDD.reduceByKey(_+_)
        val tuples: Array[(String, Int)] = wordToCountRDD.collect()
        //tuples.foreach(println)

        sc.stop()

    }
}
