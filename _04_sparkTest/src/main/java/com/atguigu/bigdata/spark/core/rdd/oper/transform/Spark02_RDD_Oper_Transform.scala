package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd = sc.makeRDD(List(1,2,3,4),2)

        // mapPartitions算子可以以一个分区为单位进行计算
        // mapPartitions算子的参数为函数类型：Iterator => Iterator
        val newRDD : RDD[Int] = rdd.mapPartitions(
            iter => {
                //println("**************")
                iter.map(_*2)
            }
        )
        newRDD.collect().foreach(println)


        sc.stop()

    }
}
