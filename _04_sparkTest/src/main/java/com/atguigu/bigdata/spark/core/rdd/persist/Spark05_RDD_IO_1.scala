package com.atguigu.bigdata.spark.core.rdd.persist

import org.apache.spark.{SparkConf, SparkContext}

object Spark05_RDD_IO_1 {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)

        println(sc.textFile("output").collect().mkString(","))
        println(sc.objectFile[(String, Int)]("output1").collect().mkString(","))
        println(sc.sequenceFile[String, Int]("output2").collect().mkString(","))


        sc.stop()

    }
}
