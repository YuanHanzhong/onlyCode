package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark10_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd   : RDD[Int] = sc.makeRDD(
            List(1,2,3,4,5,6),3
        )
        rdd.saveAsTextFile("output")
        // TODO coalesce算子用于缩减（合并）分区
        // coalesce算子可能会导致数据不均衡，因为合并数据是按照节点的位置，而不是按照数据量
        // 如果想要缩减分区后的数据均衡一些，那么就需要将数据打乱，重新组合，执行shuffle
        // coalesce算子的第二个参数就表示是否使用shuffle，默认取值为false
        val newRDD = rdd.coalesce(2, true)
        newRDD.saveAsTextFile("output1")




        sc.stop()

    }
}
