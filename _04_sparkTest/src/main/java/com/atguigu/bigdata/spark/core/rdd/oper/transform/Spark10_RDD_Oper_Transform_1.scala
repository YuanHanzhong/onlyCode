package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark10_RDD_Oper_Transform_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd   : RDD[Int] = sc.makeRDD(
            List(1,2,3,4,5,6),3
        )
        rdd.saveAsTextFile("output")
        // TODO coalesce算子用于缩减（合并）分区
        // 如果在不shuffle的情况下，使用coalesce算子扩大分区，没有意义
        // coalesce算子用于缩减分区
        //val newRDD = rdd.coalesce(6, true)
        // repartition算子表示重新分区
        // repartition算子用于扩大分区
        val newRDD = rdd.repartition(6)
        newRDD.saveAsTextFile("output1")




        sc.stop()

    }
}
