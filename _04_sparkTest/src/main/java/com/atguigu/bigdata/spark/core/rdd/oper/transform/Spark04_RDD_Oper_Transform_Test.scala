package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.{SparkConf, SparkContext}

object Spark04_RDD_Oper_Transform_Test {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd = sc.makeRDD(List(
            List(1,2), 3, List(4,5)))

        // 扁平化：将一个整体拆分成个体来使用
        //       3 => 1,1,1
        //    new User() => attr(属性)
        //    Tuple => _1, _2, _3
        rdd.flatMap{
            case list : List[_] => {
                list
            }
            case other => {
                List(other)
            }
        }.collect().foreach(println)




        sc.stop()

    }
}
