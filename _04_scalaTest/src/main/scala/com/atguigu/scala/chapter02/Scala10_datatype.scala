package com.atguigu.scala.chapter02

object Scala10_datatype {
  def main(args: Array[String]): Unit = {
    // 常量的运算在编译期间就可以进行运算.

    //val c : Char = 'A' + 1  // val c : Char = 'B'
    //println(c)


    val c : Char = 'A'

    val cc : Char = (c + 1).toChar

    println(cc )


  }
}
