package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark07_RDD_Oper_Transform_Test {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val lineRDD = sc.textFile("data/apache.log")
        lineRDD.filter(
            line => {
                val datas = line.split(" ")
                val time = datas(3)
                time.startsWith("17/05/2015")
            }
        ).map(
            line => {
                val datas = line.split(" ")
                datas(6)
            }
        ).collect().foreach(println)



        sc.stop()

    }
}
