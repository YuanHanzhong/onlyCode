package com.atguigu.bigdata.spark.core.acc


import org.apache.spark.rdd.RDD
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_Acc {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)
        val rdd = sc.makeRDD(List(1,2,3,4),2 )

        // TODO
        //  如果数据和RDD没有关系，RDD就不可能将数据拉取回Driver
        //  希望即使数据和RDD没有关系，也要在使用完毕后拉取回到Driver，RDD数据模型没有这个能力
        //  可以采用新的数据模型【累加器】实现

        // TODO 1. 声明累加器变量
        val sum: LongAccumulator = sc.longAccumulator("sum")

        rdd.foreach(
            num => {
                // TODO 2. 增加数据
                sum.add(num)
            }
        )
        // TODO 3. 获取累加的结果
        println(sum.value)

        sc.stop()

    }
}
