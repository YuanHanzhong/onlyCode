package com.atguigu.bigdata.spark.core.rdd.persist

import org.apache.spark.{SparkConf, SparkContext}

object Spark03_RDD_Persist {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)
        sc.setCheckpointDir("cp")

        val lines = sc.textFile("_04_sparkTest/data/word.txt")
        val words = lines.flatMap(_.split(" "))
        val wordToOne = words.map(
            word => {
                println("************")
                (word, 1)
            }
        )
        // TODO 持久化：只能当前App有效，不能跨越App
        // 如果想要数据可以跨越App，需要采用检查点操作
        // TODO Checkpoint directory has not been set in the SparkContext
        // 检查点为了保证数据的安全，会执行2遍, 可以联合使用cache操作，提升效率
        wordToOne.cache()
        wordToOne.checkpoint()
        val wordToCount = wordToOne.reduceByKey(_+_)
        wordToCount.saveAsTextFile("output")
        println("==========================")
        val groupWord = wordToOne.groupByKey()
        groupWord.saveAsTextFile("output1")


        sc.stop()

    }
}
