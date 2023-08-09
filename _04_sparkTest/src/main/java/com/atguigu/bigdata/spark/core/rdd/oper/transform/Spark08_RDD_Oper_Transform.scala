package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark08_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换

        // User,User,User,User
        val rdd   : RDD[Int] = sc.makeRDD(1 to 10)
        // TODO sample : 采样（抽取数据）
        //  sample算子的第一个参数表示抽取数据的方式：
        //   1.  抽取后放回：重复数据, true
        //       sample算子的第二个参数表示每条数据被抽取的次数（预计）：
       // val sampleRDD = rdd.sample(true, 2)
        //   2.  抽取后不放回：没有重复数据, false
//                  sample算子的第二个参数表示每条数据抽取的几率：
        val sampleRDD = rdd.sample(false, 0.5, 10)

        // sample还有第三个参数,表示随机数种子
        // TODO 随机数不随机:采用随机算法（MD5）
        // new Random().nextInt()
        sampleRDD.collect().foreach(println)



        sc.stop()

    }
}
