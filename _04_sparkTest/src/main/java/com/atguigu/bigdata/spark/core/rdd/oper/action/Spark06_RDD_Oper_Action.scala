package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

object Spark06_RDD_Oper_Action {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(1,2,3,4))

        // TODO
        // aggregate算子如果分区内和分区间计算规则相同，可以采用fold算子简化
        val result = rdd.aggregate(10)(_+_, _+_)
        // TODO fold算子也可以实现 WordCount( 9 / 10)
        val result1 = rdd.fold(10)(_+_)
        println(result)


        sc.stop()

    }
}
