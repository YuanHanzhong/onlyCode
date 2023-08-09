package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

object Spark07_RDD_Oper_Action {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(1,1,1,1,2,2,2))
        val rdd1 = sc.makeRDD(List(
            ("a", 1), ("a", 2), ("a", 2), ("a", 3),("a", 3),("a", 3)
        ))

        // TODO countByValue算子表示数据集中相同得值得数量
        // TODO countByValue算子可以实现WordCount (10 / 10)
        //println(rdd.countByValue())
        // TODO countByKey算子可以实现WordCount (11 / 10)
        println(rdd1.countByKey())


        sc.stop()

    }
}
