package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_Oper_Action {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(1,2,3,4), 2)

        // reduce算子先分区内计算，再分区间计算
        rdd.reduce(
            (x, y) => {
                println(x + "+" + y)
                x + y
            }
        )


        sc.stop()

    }
}
