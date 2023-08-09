package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark04_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd = sc.makeRDD(List(
            List(1,2), List(3,4)
        ))
        // 1,2,3,4
        // 扁平化：将一个整体拆分成个体来使用，全部使用
        // flatMap算子传递一个参数，参数类型为函数类型： List => Collection
        // 参数中的前面的list表示数据集中的每一条数据
        // 参数中的后面的list表示处理完成后的每一条数据的封装容器
        // List(1,2,3) => 1,2,3 => List(1,2,3)
        // List(1,2,3) => 1,2,3 => Array(1,2,3)
        rdd.flatMap(list=>list).collect.foreach(println)




        sc.stop()

    }
}
