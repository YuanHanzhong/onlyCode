package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark14_RDD_Oper_Transform_reduceByKey_got {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd= sc.makeRDD( List(
            ("a",1),("a",2),("a",9)
        ))

        // TODO reduceByKey算子需要传递一个参数，参数类型为函数类型：（Int, Int）=>Int
        //      reduceByKey算子可以实现 WordCount (2 / 10)
        // reduceByKey算子会对 [相同] key 的value进行两两聚合, 会有shuffle, 有shuffle的算子基本上会改变分区
        // (String, (Int, Int, Int))
        rdd.reduceByKey(_+_).collect().foreach(println) // got  * * *   2021/12/25 20:20  ctrl+p提示参数时, 提示的很全. 鼠标放在上面, 提示的说明比较多


        sc.stop()
    }
}
