package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark09_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd   : RDD[Int] = sc.makeRDD(
            List(1,1,1,1),2
        )

        val newRDD: RDD[Int] = rdd.distinct()
        // map(x => (x, null)).reduceByKey((x, _) => x, numPartitions).map(_._1)
        // [1,1,1,1]
        // [(1,null),(1,null),(1,null),(1,null)]
        // [1, (null, null, null, null)]
        // [1, (   null,    null, null)]
        // [1, (        null,     null)]
        // [1, null]
        // [1]

        newRDD.collect().foreach(println)



        sc.stop()

    }
}
