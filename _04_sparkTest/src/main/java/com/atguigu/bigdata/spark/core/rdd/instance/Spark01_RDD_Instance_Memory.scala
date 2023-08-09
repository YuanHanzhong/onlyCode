package com.atguigu.bigdata.spark.core.rdd.instance

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Instance_Memory {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO 创建RDD - 从内存中

        // parallelize : 并行
        val rdd : RDD[Int] = sc.parallelize(List(1,2,3,4))
        val rdd1 : RDD[Int] = sc.makeRDD(List(1,2,3,4))
        rdd.collect().foreach(println)

        sc.stop()

    }
}
