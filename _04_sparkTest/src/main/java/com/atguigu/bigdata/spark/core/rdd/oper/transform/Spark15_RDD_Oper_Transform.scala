package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark15_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",1),("a",2),("a",3)
        ))

        // TODO groupBy算子是按照指定的规则对数据进行分组
        // (a, ((a, 1), (a, 2), (a, 3)))
        val rdd1: RDD[(String, Iterable[(String, Int)])] = rdd.groupBy(_._1)
        rdd1.mapValues(_.map(_._2).sum).collect().foreach(println)
        // TODO groupByKey算子是按照数据的key进行分组
        //      groupByKey算子可以实现 WordCount (3 / 10)
        // (a, (1,2,3))
        val rdd2: RDD[(String, Iterable[Int])] = rdd.groupByKey
        rdd2.mapValues(_.sum).collect().foreach(println)


        sc.stop()
    }
}
