package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark16_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",1),("a",2),("b",3),
            ("b",4),("b",5),("a",6)
        ), 2)

        // TODO 分区内相同的key取最大值，分区间相同的key求和
        // reduceByKey的逻辑操作要求：分区内和分区间计算规则相同

        //  ("a",1),("a",2),("b",3)
        //       (a, 2)(b, 3)
        //                    (a, 8),(b, 8)
        //       (b, 5)(a, 6)
        //  ("b",4),("b",5),("a",6)
        // TODO aggregateByKey算子可以实现分区内，分区间不同的计算逻辑
        // aggregateByKey算子存在函数柯里化，有2个参数列表
        // 第一个参数列表需要传递一个参数：表示计算初始值
        // 第二个参数列表需要传递两个参数：
        //        第一个参数表示分区内计算规则
        //        第二个参数表示分区间计算规则
        // (v,v,v,v,v,v,v,v)
        rdd.aggregateByKey(5)(
            (x, y) => math.max(x, y),
            (x, y) => x + y
        ).collect.foreach(println)


        sc.stop()
    }
}
