package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SPARK_BRANCH, SparkConf, SparkContext}

object Spark18_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",1),("a",2),("b",3),
            ("b",4),("b",5),("a",6)
        ), 2)

        // 1.
        rdd.reduceByKey(_+_)
        // 2.
        rdd.aggregateByKey(0)(_+_, _+_)
        // 3.
        rdd.foldByKey(0)(_+_)
        // 4.
        //rdd.combineByKey(v=>v, (i:Int, v)=>i + v, (i:Int, j:Int)=>i + j)
        //rdd.combineByKey(v=>v, _+_, _+_)
        rdd.groupByKey()



        sc.stop()
    }
}
