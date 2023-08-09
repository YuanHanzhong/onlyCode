package com.atguigu.scala.chapter07

/**
  * Scala - 集合 - 练习1
  */
object Scala13_lianxi1 {
  def main(args: Array[String]): Unit = {

    val dataList = List(
      ("Hello Scala", 4),
      ("Hello Spark", 2),
      ("Spark Hive",3)
    )

    //思路一:
    // ("Hello Scala", 4) => "Hello Scala Hello Scala Hello Scala Hello Scala "

    val result: Map[String, Int] = dataList.map(kv => (kv._1 + " ") * kv._2)
      .flatMap(_.split(" "))
      .groupBy(word => word)
      .mapValues(_.size)
    println(result)


    //思路二:  ("Hello Scala", 4)  =>  ("Hello",4) ("Scala",4)
    val result1: Map[String, Int] = dataList.flatMap(kv => kv._1.split(" ").map((_, kv._2)))
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)
    println(result1)


  }
}
