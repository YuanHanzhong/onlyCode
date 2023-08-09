package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, Partitioner, SparkConf, SparkContext}

object Spark13_RDD_Oper_Transform_2 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd = sc.makeRDD( List(
            ("nba", 1111),
            ("cba", 2222),
            ("nba", 3333),
            ("wnba", 4444),
        ),2)
        // nba=>0,cba=>1,wnba=>2
        rdd.partitionBy( new MyPartitioner() ).saveAsTextFile("output")


        sc.stop()
    }
    // TODO 自定义分区器
    // 1. 继承Partitioner
    // 2. 重写方法
    class MyPartitioner extends Partitioner {
        // 重分区的数量
        override def numPartitions: Int = 3

        // 根据数据的key值返回所在分区的编号（从0开始）
        override def getPartition(key: Any): Int = {
            key match {
                case "nba" => 0
                case "cba" => 1
                case "wnba" => 2
            }
        }
    }
}
