package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程  - 函数作为值(对象) - 赋值给变量
  */
object Scala05_function {
  def main(args: Array[String]): Unit = {
    //1. 声明一个普通的函数

    def funValue(name : String) : String = {
      return name.toUpperCase()
    }

    //def funValue(name:String ) = name.toUpperCase

    //2. 将funValue函数整体赋值给一个变量

    var f  = funValue _

    println(f("zhangsan"))

    // 当前funValue函数的类型为: String => String
    // 当前funValue函数的返回值类型为: String

    var ff : String => String = funValue

    println(ff("lisi"))


  }
}
