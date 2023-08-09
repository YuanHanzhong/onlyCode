package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark17_RDD_Oper_Transform_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",1),("a",2),("b",3),
            ("b",4),("b",5),("a",6)
        ), 2)

        // TODO combineByKey算子可以实现 WordCount (6 / 10)
        rdd.combineByKey(
            v => v,
            (t : Int, v) => {
                t + v
            },
            (t1 : Int, t2 : Int) => {
                t1 + t2
            }
        ).collect.foreach(println)

        sc.stop()
    }
}
