package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

object Spark09_RDD_Oper_Action_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(
            1,2,3,4
        ),2)

//        var sum = 0
//        rdd.collect().foreach(
//            num => {
//                sum += num
//            }
//        )
        //println("***********************")
        var sum = 0 // Driver
        rdd.foreach( // 算子
            num => {
                sum += num // Executor
            }
        )
        println(sum) // Driver


        sc.stop()

    }
}
