package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark05_RDD_Oper_Transform_Test {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换

        val rdd   : RDD[Int] = sc.makeRDD(List(1,2,3,4), 2)
        // 【1，2】，【3，4】
        // [2],[4]
        val newRDD = rdd.glom().map(_.max)
        newRDD.sum()

        rdd.mapPartitions(
            iter => {
                List(iter.max).iterator
            }
        )





        sc.stop()

    }
}
