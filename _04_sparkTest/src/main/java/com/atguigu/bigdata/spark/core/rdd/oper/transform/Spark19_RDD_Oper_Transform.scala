package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark19_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",1),("a",6),("b",4),
            ("b",3),("b",5),("a",2)
        ), 2)

        // sortByKey算子纯粹对key进行排序，value不考虑
        rdd.sortByKey(false).collect().foreach(println)
        // a,1,a,2,a6,b,3b,4,b,5



        

        sc.stop()
    }
}
