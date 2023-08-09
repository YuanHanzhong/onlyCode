package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark21_RDD_Req {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 案例实操
        // 1. 尽快能少的使用单点集合操作
        // 2. shuffle的性能会比较差

        val lineRDD = sc.textFile("data/agent.log")

        // 1. 按照省份对数据进行分组
        val groupRDD: RDD[(String, Iterable[String])] = lineRDD.groupBy(
            line => {
                val datas = line.split(" ")
                datas(1)
            }
        )

        // 2. 组内可以统计广告（word）点击数量(count)
        val reduceRDD = groupRDD.mapValues(
            list => {
                val wordToOne = list.map(
                    line => {
                        val datas = line.split(" ")
                        (datas(4), 1)
                    }
                )
                val groupMap: Map[String, Iterable[(String, Int)]] = wordToOne.groupBy(_._1)
                val adToCount: Map[String, Int] = groupMap.mapValues(_.size)
                adToCount.toList.sortBy(_._2)(Ordering.Int.reverse).take(3)
            }
        )

        reduceRDD.collect().foreach(println)
        

        sc.stop()
    }
}
