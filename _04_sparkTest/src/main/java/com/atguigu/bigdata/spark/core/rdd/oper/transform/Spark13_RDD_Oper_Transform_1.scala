package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

object Spark13_RDD_Oper_Transform_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        //val rdd1 : RDD[Int] = sc.makeRDD( List(1,2,3,4))
        val rdd2 : RDD[(Int, Int)] = sc.makeRDD( List(
            (1,1),(2,2),(3,3),(4,4)
        ),2)

        //rdd2.sortBy()
        // 重分区
        // repartition => 改变分区（扩大分区）=> 强调分区数量
        // partitionBy => 分区的数据存储
        // partitionBy算子需要传递一个参数：表示分区器
        // Spark默认提供了三个分区器：用于决定数据所在分区
        // 常用的有2个：RangePartitioner(数据可以比较，可以排序), HashPartitioner（默认）
        rdd2.partitionBy( new HashPartitioner(2) )
            .partitionBy( new HashPartitioner(2) )
            .saveAsTextFile("output")


        sc.stop()
    }
}
