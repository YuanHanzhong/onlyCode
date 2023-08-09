package com.atguigu.bigdata.spark.core.rdd.instance

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Instance_Memory_Partition {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[4]").setAppName("RDD")
        //conf.set("spark.default.parallelism", "2")
        val sc = new SparkContext(conf)

        // TODO 创建RDD - 分区 - 16

        // makeRDD方法可以传递两个参数
        //    第一个参数表示数据集（数据源）
        //    第二个参数表示分区的数量，存在默认值
        //        totalCores 表示当前环境的总核数（最大核数）
        //        scheduler.conf.getInt("spark.default.parallelism", totalCores)
        val rdd : RDD[Int] = sc.makeRDD(List(1,2,3,4))
        // 分区保存成文本文件
        rdd.saveAsTextFile("output")

        sc.stop()

    }
}
