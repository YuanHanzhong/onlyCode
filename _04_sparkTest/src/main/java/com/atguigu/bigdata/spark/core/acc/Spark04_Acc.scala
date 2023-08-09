package com.atguigu.bigdata.spark.core.acc

import org.apache.spark.util.AccumulatorV2
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object Spark04_Acc {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)
        val rdd = sc.makeRDD(List(
            "Hello", "Spark", "Hello", "Scala"
        ),2 )

        // TODO 自定义数据累加器 - WordCount
        val wordCountAcc = new MyWordCountAccumulator()
        sc.register(wordCountAcc)

        // TODO : map & foreach
        //  1. map算子可以遍历数据集中的每一条数据，处理后需要返回
        //     map算子是转换算子，不能触发Job的执行
        //  2. foreach算子可以遍历数据集中的每一条数据，但是处理后不需要返回
        //     foreach算子行动算子，能触发Job的执行

        // TODO 特殊情况下，累加器的结果会少算
        // TODO 特殊情况下，累加器的结果会多算
        val mapRDD = rdd.map(
            word => {
                // TODO 2. 增加数据
                wordCountAcc.add(word)
            }
        )

        mapRDD.collect()
        mapRDD.collect()

        // TODO 3. 获取累加的结果
        println(wordCountAcc.value)

        sc.stop()

    }
    // TODO 自定义数据累加器
    //   1. 继承类AccumulatorV2
    //   2. 定义泛型
    //      IN : String
    //      OUT : Map[String, Int]
    //   3. 重写方法(3 + 3)
    class MyWordCountAccumulator extends AccumulatorV2[String, mutable.Map[String, Int]] {
        private val wcMap = mutable.Map[String, Int]()

        // TODO 判断是否为初始状态
        override def isZero: Boolean = {
            wcMap.isEmpty
        }

        // TODO 复制累加器
        override def copy(): AccumulatorV2[String, mutable.Map[String, Int]] = {
            new MyWordCountAccumulator()
        }

        // TODO 重置累加器
        override def reset(): Unit = {
            wcMap.clear()
        }

        // TODO 累加器用于累加数据的方法
        override def add(word: String): Unit = {
            val oldCnt = wcMap.getOrElse(word, 0)
            wcMap.update(word, oldCnt + 1)
        }

        // TODO 合并多个累加器的结果(两个Map的合并)
        override def merge(other: AccumulatorV2[String, mutable.Map[String, Int]]): Unit = {
            val otherMap = other.value
            otherMap.foreach {
                case (word, cnt) => {
                    val oldCnt: Int = wcMap.getOrElse(word, 0)
                    wcMap.update(word, oldCnt + cnt)
                }
            }
        }

        // TODO 累加器的返回结果
        override def value: mutable.Map[String, Int] = wcMap
    }
}
