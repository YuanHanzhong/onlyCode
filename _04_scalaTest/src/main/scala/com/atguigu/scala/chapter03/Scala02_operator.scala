package com.atguigu.scala.chapter03

object Scala02_operator {
  def main(args: Array[String]): Unit = {
    var str1 = new String("abc")

    var str2 = new String("abc")

    println(str1 == str2) // true    非空判断+equals
    //println(str1.equals(str2)) // true

    //println(str1.eq(str2))  //false  ==
    println(str1 eq str2)

  }
}
