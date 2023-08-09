package com.atguigu.bigdata.spark.core.rdd.persist

import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Persist {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)

        val lines = sc.textFile("data/word.txt")
        val words = lines.flatMap(_.split(" "))
        val wordToOne = words.map((_, 1))
        val wordToCount = wordToOne.reduceByKey(_+_)
        wordToCount.saveAsTextFile("output")

        val lines1 = sc.textFile("data/word.txt")
        val words1 = lines1.flatMap(_.split(" "))
        val wordToOne1 = words1.map((_, 1))
        val groupWord = wordToOne1.groupByKey()
        groupWord.saveAsTextFile("output1")


        sc.stop()

    }
}
