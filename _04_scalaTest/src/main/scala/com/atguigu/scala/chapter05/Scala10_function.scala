package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程  - 匿名函数
  */
object Scala10_function {
  def main(args: Array[String]): Unit = {

    def fValue( name : String) : String = {
      name.toUpperCase
    }

    //var f  = fValue _

    var ff : String=> String  =  {_.toUpperCase}

    println(ff("zhangsan"))


    println("---------------------------------------")


    def fun() : String => String = {

//      def freturn(name: String) : String = {
//        name.toUpperCase
//      }
//
//      freturn

      _.toUpperCase
    }

    println(fun()("lisi"))
  }
}
