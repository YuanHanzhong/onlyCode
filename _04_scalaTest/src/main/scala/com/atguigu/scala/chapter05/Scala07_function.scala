package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 函数作为值 - 当成返回值使用
  */
object Scala07_function {

  def main(args: Array[String]): Unit = {

    // 定义一个函数， 该函数的返回值类型是: 函数类型

    def fun() : String=>String  = {

      def freturn(name : String) : String = {
        name.toUpperCase
      }

      freturn
    }

    //val f: String => String = fun()
    //println(f("zhangsan"))

    println(fun()("zhangsan"))


  }
}
