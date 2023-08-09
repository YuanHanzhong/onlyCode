package com.atguigu.bigdata.spark.core.rdd.persist

import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_Persist {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)

        val lines = sc.textFile("_04_sparkTest/data/word.txt")
        val words = lines.flatMap(_.split(" "))
        val wordToOne = words.map(
            word => {
                println("************")
                (word, 1)
            }
        )
        // TODO 持久化：只能当前App有效，不能跨越App
        // 1. 缓存
        // 2. 持久化
//        wordToOne.cache()
        //wordToOne.persist()
       // wordToOne.persist(StorageLevel.)
        val wordToCount = wordToOne.reduceByKey(_+_)
        wordToCount.saveAsTextFile("output")
        println("==========================")
        val groupWord = wordToOne.groupByKey()
        groupWord.saveAsTextFile("output1")


        sc.stop()

    }
}
