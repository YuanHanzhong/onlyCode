package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark21_RDD_Req_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 案例实操
        // 1. 尽快能少的使用单点集合操作
        // 2. shuffle的性能会比较差

        // 类似的需求：先聚合再分组

        val lineRDD = sc.textFile("data/agent.log")

        // 1. 先统计wordcount
        //    word(省份，广告), count（点击数量）
        val wordRDD = lineRDD.map(
            line => {
                val datas = line.split(" ")
                ((datas(1), datas(4)), 1)
            }
        )

        val reduceRDD: RDD[((String, String), Int)] = wordRDD.reduceByKey(_ + _)

        // 2. 再分组
        //    将数据按照省份进行分组
        //    ((省份，广告), 点击数量）=> ( 省份， （广告，点击数量） )
        val mapRDD = reduceRDD.map {
//            case ((prv, ad), sum) => {
//                (prv, (ad, sum))
//            }
            t => {
                (t._1._1, (t._1._2, t._2))
            }
        }

        val prvGroupRDD: RDD[(String, Iterable[(String, Int)])] = mapRDD.groupByKey()

        val top3 = prvGroupRDD.mapValues(
            list => {
                list.toList.sortBy(_._2)(Ordering.Int.reverse).take(3)
            }
        )

        top3.collect().foreach(println)
        

        sc.stop()
    }
}
