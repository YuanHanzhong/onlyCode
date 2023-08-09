package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql.{Encoder, _}
import org.apache.spark.sql.expressions.{Aggregator, MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, IntegerType, StructField, StructType}


object Spark10_SQL_UDAF_Class_OldVersion {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()
        import spark.implicits._

        val df: DataFrame = spark.read.json("data/user.json")
        val ds: Dataset[User] = df.as[User]

        // TODO Spark3.0之前，无法将强类型的UDAF应用在SQL中
        //      可以将UDAF应用在DSL语法中
        ds.select(new MyAvgAge().toColumn).show

        spark.stop()

    }
    case class User( id:Long, name:String, age:Long )
    case class AvgAgeBuffer( var total:Long, var cnt:Long )
    // TODO 自定义聚合函数（早期版本）(强类型)
    //  1. 继承类 Aggregator
    //  2. 定义泛型
    //     IN : User
    //     BUFF : AvgAgeBuffer
    //     OUT : Int
    //  3. 重写方法（4 + 2）
    class MyAvgAge extends Aggregator[User, AvgAgeBuffer, Long] {
        override def zero: AvgAgeBuffer = {
            AvgAgeBuffer(0L, 0L)
        }

        override def reduce(buffer: AvgAgeBuffer, user: User): AvgAgeBuffer = {
            buffer.total += user.age
            buffer.cnt += 1L
            buffer
        }

        override def merge(b1: AvgAgeBuffer, b2: AvgAgeBuffer): AvgAgeBuffer = {
            b1.total += b2.total
            b1.cnt += b2.cnt
            b1
        }

        override def finish(buffer: AvgAgeBuffer): Long = {
            buffer.total / buffer.cnt
        }

        override def bufferEncoder: Encoder[AvgAgeBuffer] = Encoders.product
        override def outputEncoder: Encoder[Long] = Encoders.scalaLong
    }
}
