package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 惰性函数
  */
object Scala15_function {
  def main(args: Array[String]): Unit = {

    def fun() : String  = {

      println("fun.....")

      "data"

    }


    println("业务处理中.......")

    lazy val result: String = fun ()

    println("业务处理中.......")

    println(result)


  }
}
