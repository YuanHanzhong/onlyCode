package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark16_RDD_Oper_Transform_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",1),("a",2),("b",3),
            ("b",4),("b",5),("a",6)
        ), 2)

        // TODO aggregateByKey算子可以实现 WordCount (4 / 10)
        //rdd.aggregateByKey(0)(_+_, _+_).collect.foreach(println)
        // TODO 如果aggregateByKey算子中分区内和分区间的计算规则相同的话，可以使用foldByKey简化
        // TODO foldByKey算子可以实现 WordCount (5 / 10)
        rdd.foldByKey(0)(_+_).collect.foreach(println)


        sc.stop()
    }
}
