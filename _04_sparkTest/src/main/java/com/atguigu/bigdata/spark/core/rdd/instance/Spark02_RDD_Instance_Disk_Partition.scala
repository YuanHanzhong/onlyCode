package com.atguigu.bigdata.spark.core.rdd.instance

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_Instance_Disk_Partition {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO 创建RDD - 从磁盘中

        // textFile方法存在两个参数
        //   第一个参数表示文件路径
        //   第二个参数表示最小分区数量，存在默认值
        //       math.min(defaultParallelism, 2)

        // 分区计算：  10%
        // Spark不能读文件的，但是文件操作都来自于Hadoop
        //      13 / 3 = 4...1
        //
        val rdd: RDD[String] = sc.textFile("data/test.txt", 3)
        rdd.saveAsTextFile("output")

        sc.stop()

    }
}
