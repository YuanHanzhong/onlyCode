package com.atguigu.bigdata.spark.core.rdd.oper.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark20_RDD_Oper_Transform {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        // TODO RDD - 算子 - 转换
        val rdd1 : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",1),("a",2),("a",3),
        ))
        val rdd2 : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",4),("a",5),("a",6)
        ))
        val rdd3 : RDD[(String, Int)] = sc.makeRDD( List(
            ("a",7),("a",8),("a",9)
        ))
        // join : SQL连接数据（关系）
        // 相同的key可以使用join算子进行连接
        // join : 可能会产生笛卡尔乘积
        // join : 可能有shuffle的
        //rdd1.join(rdd2).collect().foreach(println)
        //rdd1.leftOuterJoin(rdd2).collect().foreach(println)
        //rdd1.rightOuterJoin(rdd2).collect().foreach(println)
        //rdd1.fullOuterJoin(rdd2).collect().foreach(println)
        // connect + group
        rdd1.cogroup(rdd2, rdd3).collect().foreach(println)

        

        sc.stop()
    }
}
