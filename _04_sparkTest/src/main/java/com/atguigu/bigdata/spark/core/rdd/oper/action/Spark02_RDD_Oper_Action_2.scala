package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object Spark02_RDD_Oper_Action_2 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(
            1,2,3,4
        ), 2)
        rdd.reduce(
            (x, y) => {
                println(Thread.currentThread().getName)
                x + y
            }
        )



        sc.stop()

    }
}
