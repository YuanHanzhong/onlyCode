package com.atguigu.bigdata.spark.core.rdd.persist

import org.apache.spark.{SparkConf, SparkContext}

object Spark04_RDD_Persist {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)
        sc.setCheckpointDir("cp")

        val lines = sc.textFile("_04_sparkTest/data/word.txt")
        val words = lines.flatMap(_.split(" "))
        val wordToOne = words.map(
            word => {
                //println("************")
                (word, 1)
            }
        )
        // cache操作会在血缘关系上增加依赖
        // checkpoint操作会切断血缘关系
        wordToOne.checkpoint()
        val wordToCount = wordToOne.reduceByKey(_+_)
        println(wordToCount.toDebugString)
        wordToCount.collect
        println("==========================")
        println(wordToCount.toDebugString)


        sc.stop()

    }
}
