package com.atguigu.bigdata.spark.core.test

object TestFlat {

    def main(args: Array[String]): Unit = {
        val list = List("Hello World")
        println(list.flatten)
        println(list.flatMap(s => s.split(" ")))
    }
}
