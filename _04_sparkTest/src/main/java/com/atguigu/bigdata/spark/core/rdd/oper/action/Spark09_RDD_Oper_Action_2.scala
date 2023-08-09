package com.atguigu.bigdata.spark.core.rdd.oper.action

import org.apache.spark.{SparkConf, SparkContext}

object Spark09_RDD_Oper_Action_2 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 行动
        val rdd = sc.makeRDD(List[Int](),2)

        // Scala闭包
        // 函数使用了外部得数据，并改变外部数据得生命周期
        // 将外部数据包含到函数得内部，形成一个闭合得环境，这个环境称之为闭包环境，简称为闭包
        // Executor端使用Driver端得数据，肯定有闭包，在执行时，提交程序前，可以直接做校验。
        // 这个校验功能称之为闭包检测（运行Job前提前进行闭包检查）
        val user = new User() // Driver
        rdd.foreach(
            num => {
                println(user.age + num) // Executor
            }
        )

        sc.stop()

    }
    // TODO : Task not serializable
    class User {
        var age : Int = 10
    }
}
