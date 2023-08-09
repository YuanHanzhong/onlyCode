package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark06_RDD_Oper_Transform_Test1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换

        val rdd   : RDD[String] = sc.makeRDD(List("Hello", "hive", "hbase", "Hadoop"))

        rdd.groupBy(word=>word.substring(0,1)).collect().foreach(println)




        sc.stop()

    }
}
