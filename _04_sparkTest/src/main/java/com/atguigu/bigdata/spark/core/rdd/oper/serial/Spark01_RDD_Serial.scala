package com.atguigu.bigdata.spark.core.rdd.oper.serial

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Serial {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        val rdd = sc.makeRDD(
            List(
                "Hello", "Hive", "Spark", "Scala"
            )
        )
        //rdd.filter(_.startsWith("H")).collect().foreach(println)
        val s = new Search("S")
        s.matchData(rdd).collect().foreach(println)


        sc.stop()
    }
    class Search( query : String ) {
        def matchData( rdd : RDD[String] ) = {
            val q : String = this.query
            rdd.filter(_.startsWith(q))
        }
    }
//    class Emp(name:String) {
//        def test(): Unit = {
//            println(this.name)
//        }
//    }
}
