package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark19_RDD_Oper_Transform_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd : RDD[(User, Int)] = sc.makeRDD( List(
            (new User(),1),(new User(),6),(new User(),4),
            (new User(),3),(new User(),5),(new User(),2)
        ), 2)

        rdd.sortByKey(false).collect().foreach(println)

        sc.stop()
    }
    class User extends Ordered[User]{
        var age : Int = _

        override def compare(that: User): Int = {
            -1
        }
    }
}
