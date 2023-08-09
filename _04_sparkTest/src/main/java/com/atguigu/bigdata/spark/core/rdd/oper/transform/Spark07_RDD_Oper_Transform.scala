package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark07_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换

        // User,User,User,User
        val rdd   : RDD[Int] = sc.makeRDD(List(1,2,3,4))

        // TODO filter
        // filter算子 ： 将数据集中的每一条数据进行筛选过滤（满足条件）
        //      如果满足，那么数据保留，如果不满足，数据丢弃
        // filter算子可能会产生数据倾斜
        rdd.filter(_ % 2 == 0).collect.foreach(println)



        sc.stop()

    }
}
