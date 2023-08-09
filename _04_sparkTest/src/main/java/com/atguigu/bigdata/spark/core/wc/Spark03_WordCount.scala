package com.atguigu.bigdata.spark.core.wc

import org.apache.spark.{SparkConf, SparkContext}

object Spark03_WordCount {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)

        // 绝对路径 : 不可改变的路径
        //      网络路径：协议：//IP地址：端口号/路径 => http://192.168.0.10:8080/test/test.html
        //      本地路径：协议：//路径 => file:///c:/test/test.html
        // 相对路径 : 可以改变的路径， 一定存在基准路径
        //      ../   ./
        //     IDEA中默认的基准路径为项目的根路径，可以改
        println(System.getProperty("user.dir"))
        val lines = sc.textFile("_04_sparkTest/data/word.txt")
        val words = lines.flatMap(_.split(" "))
        val wordGroup = words.groupBy(word => word)
        val wordCount = wordGroup.mapValues(_.size)
        wordCount.collect().foreach(println)

        sc.stop()

    }
}
