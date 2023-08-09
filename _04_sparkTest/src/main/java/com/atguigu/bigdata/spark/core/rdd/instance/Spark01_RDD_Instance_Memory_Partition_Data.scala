package com.atguigu.bigdata.spark.core.rdd.instance

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Instance_Memory_Partition_Data {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO 创建RDD - 分区 - 数据存储
        // makeRDD可以将数据根据分区数量进行分配：一般的原则就是均分
        // 【1，2】【3，4】
        // 【1，2】【3，4,5】
        // 【1】【2，3】【4，5】

        val rdd : RDD[Int] = sc.makeRDD(List(1,2,3,4,5), 3)
        // 分区保存成文本文件
        rdd.saveAsTextFile("output")

        sc.stop()

    }
}
