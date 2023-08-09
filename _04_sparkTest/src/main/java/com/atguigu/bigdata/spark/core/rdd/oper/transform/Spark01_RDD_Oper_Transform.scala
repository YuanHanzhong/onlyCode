package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd = sc.makeRDD(List(1,2,3,4))
        // TODO map算子
        // map算子的目的是将数据集中的每一条数据进行转换映射（A => B）
        // map算子需要传递一个参数，参数的类型为函数类型： Int => U(不确定)
//        def mapFunction( num : Int ): Int = {
//            num * 2
//        }
//
//        val newRDD : RDD[Int] = rdd.map(mapFunction)
        val newRDD : RDD[Int] = rdd.map(_ * 2)
        newRDD.collect().foreach(println)


        sc.stop()

    }
}
