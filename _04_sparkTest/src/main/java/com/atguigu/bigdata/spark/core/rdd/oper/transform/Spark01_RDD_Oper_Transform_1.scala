package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Oper_Transform_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd = sc.makeRDD(List(1,2,3,4), 2)
        // TODO map算子
        rdd.saveAsTextFile("output")
        val newRDD : RDD[Int] = rdd.map(_ * 2)
        //newRDD.collect().foreach(println)
        newRDD.saveAsTextFile("output1")


        sc.stop()

    }
}
