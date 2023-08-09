package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{DataFrame, Encoder, Encoders, Row, SparkSession, functions}

object Spark08_SQL_UDAF {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        val df: DataFrame = spark.read.json("data/user.json")
        df.createTempView("user")
        // SQL => 弱类型，没有类型的概念
        // Aggregator => 强类型
        // 强类型无法应用在弱类型操作上，Aggregator不能用在SQL文中的。
        // Spark3.0开始，可以通过特殊转换，可以将Aggregator应用在SQL文
        spark.udf.register("avgAge", functions.udaf(new MyAvgAge()))
        spark.sql("select avgAge(age) from user").show

        spark.stop()

    }
    case class AvgAgeBuffer( var total:Int, var cnt:Int )
    // TODO 自定义聚合函数（UDAF）
    // 1. 继承类 ： org.apache.spark.sql.expressions.Aggregator
    // 2. 定义泛型 ：
    //    IN : Int
    //    BUFF : (Total, Cnt) => AvgAgeBuffer
    //    OUT : Int
    // 3. 重写方法（4 + 2）
    class MyAvgAge extends Aggregator[Int, AvgAgeBuffer, Int] {
        // TODO 缓冲区的初始化
        override def zero: AvgAgeBuffer = {
            AvgAgeBuffer(0, 0)
        }

        // TODO 将输入的年龄更新缓冲区
        override def reduce(buffer: AvgAgeBuffer, age: Int): AvgAgeBuffer = {
            buffer.total += age
            buffer.cnt += 1
            buffer
        }

        // TODO 将多个缓冲区数据进行合并
        override def merge(b1: AvgAgeBuffer, b2: AvgAgeBuffer): AvgAgeBuffer = {
            b1.total += b2.total
            b1.cnt += b2.cnt
            b1
        }

        // TODO 获取聚合函数的结果
        override def finish(buffer: AvgAgeBuffer): Int = {
            buffer.total / buffer.cnt
        }

        override def bufferEncoder: Encoder[AvgAgeBuffer] = Encoders.product
        override def outputEncoder: Encoder[Int] = Encoders.scalaInt
    }
}
