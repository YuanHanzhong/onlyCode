package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark14_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",1),("a",2),("a",3)
        ))

        // TODO reduceByKey算子需要传递一个参数，参数类型为函数类型：（Int, Int）=>Int
        //      reduceByKey算子可以实现 WordCount (2 / 10)
        // reduceByKey算子会对相同key 的value进行两两聚合
        // (String, (Int, Int, Int))
        rdd.reduceByKey(_+_).collect().foreach(println)


        sc.stop()
    }
}
