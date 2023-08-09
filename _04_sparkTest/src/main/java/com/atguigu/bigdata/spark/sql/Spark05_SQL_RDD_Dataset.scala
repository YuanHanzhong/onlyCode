package com.atguigu.bigdata.spark.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object Spark05_SQL_RDD_Dataset {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()
        // RDD, DataFrame, Dataset之间的转换需要使用隐式转换
        import spark.implicits._
        // TODO 获取RDD(SparkContext)
        // Tuple的顺序号，其实就是Tuple对象的属性
        val rdd = spark.sparkContext.makeRDD(
            List(
                (1, "zhangsan", 30),
                (2, "lisi", 40),
                (3, "wangwu", 50),
            )
        )
        // TODO 将RDD转换为Dataset
        val ds: Dataset[User] = rdd.map {
            case (id, name, age) => {
                User(id, name, age)
            }
        }.toDS()

        // TODO 将Dataset转换为RDD
        val rdd1: RDD[User] = ds.rdd

        ds.show

        spark.stop()

    }
    case class User( id:Int, name:String, age:Int )
}
