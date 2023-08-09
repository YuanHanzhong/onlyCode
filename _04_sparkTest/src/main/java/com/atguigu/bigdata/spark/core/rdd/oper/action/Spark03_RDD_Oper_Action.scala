package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

object Spark03_RDD_Oper_Action {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(3, 4,1, 2), 2)
        val rdd1 = sc.textFile("hdfs://xxxxxxx", 2)

        // collect算子是行动算子
        // collect算子是按照分区编号采集,并合并
        // collect算子会返回数组, 可能会对Driver得资源造成压力
        rdd.collect()


        sc.stop()

    }
}
