package com.atguigu.scala.chapter07

/**
  * Scala - 集合 - 并行集合
  */
object Scala11_par {
  def main(args: Array[String]): Unit = {
    
    val range: Range.Inclusive = 1 to 100

    println(range.map(num => Thread.currentThread().getName))  // main

    println(range.par.map(num => Thread.currentThread().getName))

  }
}
