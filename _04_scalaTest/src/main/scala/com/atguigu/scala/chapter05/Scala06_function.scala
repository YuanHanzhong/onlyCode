package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 函数作为值 - 当成参数使用
  */
object Scala06_function {
  def main(args: Array[String]): Unit = {

    //1. 声明函数， 函数的参数类型为: 函数类型

    def fun( f : String => String  ) : Unit = {
      println(f("zhangsan"))
    }

    // 函数类型:  String =>String
    def fparam(name:String ) :String = {
      name.toUpperCase
    }

    fun(fparam)

    println("-------------------------------------")


    def fun1(f: (String, Int) => String ) : String ={
      f("*",5)
    }

    def fparam1(str:String, num:Int ) : String ={
      str * num
    }


    println(fun1( fparam1 ))
  }

  def testMethod(f: String=>String):Unit = {

  }
}
