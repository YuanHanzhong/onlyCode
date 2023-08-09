package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.reflect.ClassTag

object Spark11_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val user1 = new User()
        user1.age = 30
        user1.money = 1000
        val user2 = new User()
        user2.age = 30
        user2.money = 2000
        val user3 = new User()
        user3.age = 30
        user3.money = 1500
        val user4 = new User()
        user4.age = 40
        user4.money = 2000
        val rdd   : RDD[User] = sc.makeRDD(
            List(user1, user2,user3, user4)
        )

        // sortBy算子表示按照指定的排序规则对数据进行排序，默认为升序
        // sortBy算子的第二个参数表示升序和降序
        // Tuple排序
        val sortRDD = rdd.sortBy(user=>(user.age, user.money))( Ordering.Tuple2( Ordering.Int, Ordering.Int.reverse ), ClassTag.apply(classOf[Tuple2[Int, Int]]) )
        sortRDD.collect().foreach(println)


        sc.stop()

    }
    case class User() {
        var age : Int = _
        var money : Int = _
        var id : Int = _

        override def toString: String = {
            s"""
              |User[${age}, ${money}]
              |""".stripMargin
        }
    }
}
