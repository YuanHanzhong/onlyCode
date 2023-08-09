package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object Spark02_RDD_Oper_Action_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List(
            "Hello", "Scala", "Hello", "Spark"
        ), 2)

        // TODO
        //  reduce算子 : 行动算子，会马上执行
        //             不需要考虑数据类型得
        //             参数类型为 ： （String(Map), String(Map)） => String(Map)
        //             算子得返回结果类型为String(Map)
        //             可以实现WordCount (7 / 10)
        //  reduceByKey算子 : 转换算子，不会马上执行
        //             处理得数据类型必须为KV类型
        //             可以实现WordCount
        // "Hello" => Map((Hello, 1))
        rdd.map(
            word => {
                mutable.Map((word, 1))
            }
        ).reduce(
            (map1, map2) => {
                map2.foreach {
                    case (word, cnt) => {
                        val oldCnt = map1.getOrElse(word, 0)
                        map1.update(word, oldCnt + cnt)
                    }
                }
                map1
            }
        ).foreach(println)


        sc.stop()

    }
}
