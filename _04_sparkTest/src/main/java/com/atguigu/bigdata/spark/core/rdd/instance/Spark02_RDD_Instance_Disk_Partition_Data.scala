package com.atguigu.bigdata.spark.core.rdd.instance

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_Instance_Disk_Partition_Data {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO 创建RDD - 从磁盘中
        // 7 / 2 = 3...1
        /* 分区计算时，预计的数据读取方案
          [0, 3]
          [3, 6]
          [6, 7]
         */

        // Spark中的文件处理就是Hadoop文件处理
        // Hadoop文件操作中，分区数量的计算和分区数据读取的计算是不一样的。
        // 分区数量的计算是按照字节计算
        // 分区数据的读取是按照特定规则：
        // 1. 是按行读取,一读取就是一行数据
        // 2. 是按照偏移量读取
        // 3. 相同的偏移量不能被重复读取
        /*

          1@@ => 012
          2@@ => 345
          3   => 6


         */

        val rdd: RDD[String] = sc.textFile("data/test.txt", 3)
        rdd.saveAsTextFile("output")

        sc.stop()

    }
}
