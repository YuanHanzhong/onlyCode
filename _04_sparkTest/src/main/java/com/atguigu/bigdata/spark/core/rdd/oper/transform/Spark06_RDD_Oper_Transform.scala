package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark06_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换

        // User,User,User,User
        val rdd   : RDD[Int] = sc.makeRDD(List(1,2,3,4), 2)

        // groupBy算子：将数据按照指定的规则进行分组
        // groupBy算子可以传递一个参数，类型为函数类型 ：Int => K(维度标记)(组名)
//        rdd.groupBy(
//            num => {
//                if ( num % 2 == 0 ) {
//                    "偶数"
//                } else {
//                    "奇数"
//                }
//            }
//        ).collect.foreach(println)

        val value: RDD[(Int, Iterable[Int])] = rdd.groupBy(
            num => {
                num % 2
            }
        )
        value.collect.foreach(println)




        sc.stop()

    }
}
