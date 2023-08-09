package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark06_RDD_Oper_Transform_Test2 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val lineRDD = sc.textFile("data/apache.log")

        // TODO groupBy算子可以实现 WordCount 功能 （1 / 10）
        val timeClickRDD = lineRDD.map(
            line => {
                val datas = line.split(":")
                (datas(1), 1)
            }
        ).groupBy(_._1).mapValues(_.size)

        timeClickRDD.collect().foreach(println)



        sc.stop()

    }
}
