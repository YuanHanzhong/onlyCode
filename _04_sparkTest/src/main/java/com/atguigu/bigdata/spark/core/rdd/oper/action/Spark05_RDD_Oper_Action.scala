package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

object Spark05_RDD_Oper_Action {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(1,2,3,4))

        // TODO
        //  aggregateByKey : 转换算子，数据类型是KV类型
        //                   计算初始值只会再分区内计算时使用
        //                   // 【10，1，2】【10，3，4】 => 30
        //  aggregate : 行动算子，数据类型没有要求
        //                   计算初始值在分区内，分区间计算时都会使用
        //                   10 + 【10，1，2】+【10，3，4】 => 40
        // 160 + 10 + 10 = 180
        val result = rdd.aggregate(10)(_+_, _+_)
        println(result)


        sc.stop()

    }
}
