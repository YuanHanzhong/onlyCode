package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.reflect.ClassTag

object Spark12_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd1 = sc.makeRDD(
            List(1,2,3,4),2
        )
        val rdd2 = sc.makeRDD(
            List(3,4,5,6),2
        )
        val rdd3 = sc.makeRDD(
            List("3", "4", "5", "6"),2
        )

        // 交集，并集，差集
        //println(rdd1.intersection(rdd2).collect().mkString(",")) // 3, 4
        //println(rdd1.intersection(rdd3).collect().mkString(",")) // 3, 4
        //println(rdd1.union(rdd2).collect().mkString(",")) // 1,2,3,4,3,4,5,6
        //println(rdd1.union(rdd3).collect().mkString(",")) // 1,2,3,4,3,4,5,6
        //println(rdd1.subtract(rdd2).collect().mkString(",")) // 1,2
        //println(rdd1.subtract(rdd3).collect().mkString(",")) // 1,2

        // 拉链
        // TODO : Can only zip RDDs with same number of elements in each partition
        // TODO : Can't zip RDDs with unequal numbers of partitions: List(2, 3)
        println(rdd1.zip(rdd2).collect().mkString(","))
        //println(rdd1.zip(rdd3).collect().mkString(","))


        sc.stop()
    }
}
