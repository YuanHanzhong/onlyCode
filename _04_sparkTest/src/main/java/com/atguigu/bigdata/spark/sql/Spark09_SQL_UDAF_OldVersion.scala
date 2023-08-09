package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql.expressions.{Aggregator, MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql._
import org.apache.spark.sql.types.{DataType, IntegerType, StructField, StructType}

object Spark09_SQL_UDAF_OldVersion {

    def main(args: Array[String]): Unit = {

        // TODO SparkSQL 环境

        val spark = SparkSession.builder
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        val df: DataFrame = spark.read.json("data/user.json")
        df.createTempView("user")
        spark.udf.register("avgAge", new MyAvgAge())
        spark.sql("select avgAge(age) from user").show

        spark.stop()

    }
    // TODO 自定义聚合函数（早期版本）
    //  1. 继承类 UserDefinedAggregateFunction
    //  2. 重写方法
    class MyAvgAge extends UserDefinedAggregateFunction {
        // TODO 输入数据的结构
        override def inputSchema: StructType = {
            StructType(
                Array(
                    StructField("age", IntegerType)
                )
            )
        }

        // TODO 缓冲区数据的结构
        override def bufferSchema: StructType = {
            StructType(
                Array(
                    StructField("total", IntegerType),
                    StructField("cnt", IntegerType)
                )
            )
        }

        // TODO 输出数据的类型
        override def dataType: DataType = {
            IntegerType
        }

        // TODO 计算的稳定性
        override def deterministic: Boolean = true

        // TODO 缓冲区结构的初始化
        override def initialize(buffer: MutableAggregationBuffer): Unit = {
            buffer.update(0, 0)
            buffer.update(1, 0)
        }

        // TODO 使用输入的数据更新缓冲区
        override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
            buffer.update(0, buffer.getInt(0) + input.getInt(0))
            buffer.update(1, buffer.getInt(1) + 1)
        }

        // TODO 合并缓冲区
        override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
            buffer1.update(0, buffer1.getInt(0) + buffer2.getInt(0))
            buffer1.update(1, buffer1.getInt(1) + buffer2.getInt(1))
        }

        // TODO 计算
        override def evaluate(buffer: Row): Any = {
            buffer.getInt(0) / buffer.getInt(1)
        }
    }
}
