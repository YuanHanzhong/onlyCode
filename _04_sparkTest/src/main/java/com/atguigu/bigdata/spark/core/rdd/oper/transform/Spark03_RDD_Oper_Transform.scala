package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark03_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd = sc.makeRDD(List(1,2,3,4),2)
        val newRDD : RDD[Int] = rdd.mapPartitionsWithIndex(
            (ind, iter) => {
                if ( ind == 1 ) {
                    iter
                } else {
                    Nil.iterator
                }
            }
        )
        newRDD.collect().foreach(println)


        sc.stop()

    }
}
