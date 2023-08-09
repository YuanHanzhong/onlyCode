package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object Spark05_RDD_Oper_Action_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(
            "Hello", "Hello", "Spark"
        ))

        // TODO aggregate算子可以实现 WordCount ( 8 / 10 )
        val mergeMap = rdd.aggregate(
            mutable.Map[String, Int]()
        )(
            (map, word) => {
                val oldCnt = map.getOrElse(word, 0)
                map.update(word, oldCnt + 1)
                map
            },
            (map1, map2) => {
                map2.foreach {
                    case (word, cnt) => {
                        val oldCnt = map1.getOrElse(word, 0)
                        map1.update(word, oldCnt + cnt)
                    }
                }
                map1
            }
        )
        println(mergeMap)


        sc.stop()

    }
}
