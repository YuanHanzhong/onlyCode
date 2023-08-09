package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark13_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd1 : RDD[Int] = sc.makeRDD( List(1,2,3,4))
        val rdd2 : RDD[(Int, Int)] = sc.makeRDD( List(
            (1,1),(2,2),(3,3)
        ))

        // 方法能不能用，取决于对象的类型,和泛型无关
        // 隐式转换的核心：类型的转换
        //rdd1.partitionBy
        //rdd2.partitionBy()


        sc.stop()
    }
}
