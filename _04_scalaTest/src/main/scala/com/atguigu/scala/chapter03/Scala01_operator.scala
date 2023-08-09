package com.atguigu.scala.chapter03

/**
  * Scala 运算符 - 本质
  *
  * Scala中没有运算符， 所谓的运算符都是方法.
  */
object Scala01_operator {

  def main(args: Array[String]): Unit = {

    var num1 : Int  = 10
    var num2 : Int  = 20

    //var sum  = num1 + num2
    //var sum  = num1. +(num2)
    //var sum = num1 + (num2)
    var sum = num1 + num2

    println(sum)

  }

}
