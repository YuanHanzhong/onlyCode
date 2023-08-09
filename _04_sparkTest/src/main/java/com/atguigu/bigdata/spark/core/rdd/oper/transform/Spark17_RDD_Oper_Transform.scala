package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark17_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",1),("a",2),("b",3),
            ("b",4),("b",5),("a",6)
        ), 2)

        // TODO 获取相同key的平均值
        // ("a",(1,1)),("a",(2,1)),("b",(3,1))
        // ("b",(4,1)),("b",(5,1)),("a",(6,1))

        // (a, List( (1,1), 2, 6 ))
        // (a, List(    (3,2), 6 ))
        // (a, (9,3))
//        rdd.map {
//            case ( word, total ) => {
//                ( word, (total, 1) )
//            }
//        }.reduceByKey(
//            (t1, t2) => {
//                ( t1._1 + t2._1, t1._2 + t2._2 )
//            }
//        ).mapValues {
//            case (total, cnt) => {
//                total / cnt
//            }
//        }.collect.foreach(println)

        // combineByKey算子需要传递三个参数：
        // 第一个参数表示：当碰见第一个key的value时，该如何处理
        // 第二个参数表示：分区内计算规则
        // 第三个参数表示：分区间计算规则
        rdd.combineByKey(
            v => (v, 1),
            (t : (Int, Int), v) => {
                (t._1 + v, t._2 + 1)
            },
            (t1 : (Int, Int), t2 : (Int, Int)) => {
                (t1._1 + t2._1, t1._2 + t2._2)
            }
        ).collect.foreach(println)

        sc.stop()
    }
}
