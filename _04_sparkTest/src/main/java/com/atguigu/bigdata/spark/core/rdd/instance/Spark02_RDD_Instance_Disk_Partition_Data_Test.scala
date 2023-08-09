package com.atguigu.bigdata.spark.core.rdd.instance

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_Instance_Disk_Partition_Data_Test {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO 创建RDD - 从磁盘中
        // 22 / 3...7...1 => 3 + 1 = 4
        /*
          [0, 7]
          [7, 14]
          [14, 21]
          [21, 22]
         */

        /*
        sunck@@   => 0123456
        is@@      => 78910
        a good@@  => 1112131415161718
        man       => 192021


         */

        val rdd: RDD[String] = sc.textFile("data/test.txt", 3)
        rdd.saveAsTextFile("output")

        sc.stop()

    }
}
