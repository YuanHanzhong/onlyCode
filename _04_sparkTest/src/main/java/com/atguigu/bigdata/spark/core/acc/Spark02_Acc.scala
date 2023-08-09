package com.atguigu.bigdata.spark.core.acc

import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_Acc {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)
        val rdd = sc.makeRDD(List(1,2,3,4),2 )

        // TODO 累加器：（增加（采集）数值，数据）
        val sum: LongAccumulator = sc.longAccumulator("sum")
        //sc.doubleAccumulator()
        //sc.collectionAccumulator()

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
