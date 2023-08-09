package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

object Spark04_RDD_Oper_Action {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(1,3,4,2), 2)

        println(rdd.count()) // 返回数量
        println(rdd.first()) // 返回结果得第一个
        println(rdd.take(3)) // 返回结果得前几个
        // [1,2,3,4] => [1,2,3]
        // [1,3,4,2] => [1,3,4]
        println(rdd.takeOrdered(3).mkString(",")) // 返回结果得前几个


        sc.stop()

    }
}
