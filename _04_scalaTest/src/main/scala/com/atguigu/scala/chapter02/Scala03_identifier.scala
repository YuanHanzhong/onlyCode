package com.atguigu.scala.chapter02

/**
  *  Scala标识符
  */
object  Scala03_identifier {
  def main(args: Array[String]): Unit = {
    /*
      Java的标识符:
         1. 字母和数组的组合， 数字不能开头
         2. 可以使用一些特殊字符，例如: _  $
         3. 遵循驼峰命名法

     Scala的标识符:
         1. 字符 a-z 数字0-9 , 数字不能开头
         2. 符号 , 想用啥就写啥, 报错换一个.
         3. 遵循驼峰命名法

     */

    var name = 1
    var name1 = 2
    //var 2name = 3

    var ~ = 1
    var ! = 1
    //var @ = 1
    var @@ = 1
    //var # = 1
    var ## = 1
    var $ = 1
    var % = 1
    var ^ = 1
    var & = 1
    var * = 1
    //var ( = 1
    //var : = 1

    var :: = 1

    var < = 1
    var > = 1

    // ..........

    :->()

    var `class` = 1

  }

  def :->() : Unit = {
    println("haha")
  }

}
