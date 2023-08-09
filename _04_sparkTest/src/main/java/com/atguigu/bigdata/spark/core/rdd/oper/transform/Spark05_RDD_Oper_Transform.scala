package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark05_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        // 1 => N
        // N => 1
        val rdd   : RDD[Int] = sc.makeRDD(List(1,2,3,4), 2)
        // glom算子可以将一个分区的数据转换为数组
        // 2,1,4,3
        // 4,3,2,1
        val newRDD: RDD[Array[Int]] = rdd.glom()
        newRDD.collect().foreach(println)




        sc.stop()

    }
}
