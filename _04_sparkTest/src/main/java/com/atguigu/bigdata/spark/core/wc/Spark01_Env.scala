package com.atguigu.bigdata.spark.core.wc

import org.apache.spark.{SparkConf, SparkContext}

object Spark01_Env {

    def main(args: Array[String]): Unit = {

        // TODO Spark 环境
        // JDBC Connection(url, user, password)
        val conf = new SparkConf().setMaster("local").setAppName("WordCount")
        val sc = new SparkContext(conf)
        
        
        

        sc.stop()

    }
}
