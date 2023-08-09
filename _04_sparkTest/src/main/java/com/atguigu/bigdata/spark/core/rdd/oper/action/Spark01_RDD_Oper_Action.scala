package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Oper_Action {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(1,2,3,4))

        // TODO 所谓得行动算子，其实就是让业务功能（Job）开始执行
        // collect算子就是一个行动算子，触发一个新得Job得执行
        val newRDD : RDD[Int] = rdd.map(_ * 2)
        newRDD.collect().foreach(println)
        newRDD.collect().foreach(println)


        sc.stop()

    }
}
