package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

object Spark09_RDD_Oper_Action {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(
            1,2,3,4
        ),2)

        rdd.collect().foreach(println)
        println("***********************")
        rdd.foreach(println)


        sc.stop()

    }
}
