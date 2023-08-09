package com.atguigu.bigdata.spark.core.rdd.instance

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_Instance_Disk {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO 创建RDD - 从磁盘中

        // textFile方法可以传递一个参数，参数表示文件路径（可以使用逗号分隔多个路径）
        // textFile方法可以传递一个参数，参数可以为路径的名称
        // textFile方法可以传递一个参数，参数可以使用通配符
        //val rdd1: RDD[String] = sc.textFile("data/word.txt,data/word1.txt")
        //val rdd2: RDD[String] = sc.textFile("data")
        //val rdd3: RDD[String] = sc.textFile("data/word*.txt")
        // 如果想要获取文件内容的同时关联文件本身，那么需要采用其他方法
        // wholeTextFiles方法会返回元组类型的数据，第一个值表示文件路径，第二个值表示文件内容
        val rdd4: RDD[(String, String)] = sc.wholeTextFiles("data/word*.txt")
        rdd4
          .collect()
          .foreach(println)

        sc.stop()

    }
}
