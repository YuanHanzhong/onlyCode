package com.atguigu.bigdata.spark.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object Spark06_SQL_DataFrame_Dataset {

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
        // TODO 将RDD转换为DataFrame
        val df : DataFrame = rdd.toDF("id", "name", "age")

        // TODO 将DataFrame转换为Dataset
        // TODO DataFrame和Dataset本质的区别：颜色不一样，
        // DataFrame其实就是特定泛型的Dataset
        val ds: Dataset[User] = df.as[User]

        // TODO 将Dataset转换为DataFrame
        val df1: DataFrame = ds.toDF()
        df1.show

        // TODo 将DataFrame 转换成RDD
        val rdd1: RDD[Row] = df1.rdd

        // rdd[Row] = Dataset[Row].rdd
        // rdd[User] = Dataset[User].rdd

        spark.stop()

    }
    case class User( id:Int, name:String, age:Int )
}
