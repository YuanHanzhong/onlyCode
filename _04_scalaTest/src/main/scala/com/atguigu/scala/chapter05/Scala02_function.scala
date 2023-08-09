package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 函数定义
  */
object Scala02_function {

  def main(args: Array[String]): Unit = {
    //1. 无参数，无返回值
    def fun1(): Unit ={
      println("fun1.....")
    }
    fun1()

    //2. 有参数，无返回值

    def fun2(name : String ) :Unit = {
      println(s"fun2..... $name")
    }
    fun2("zhangsan")


    //3. 无参数, 有返回值
    def fun3() : String = {

      return "fun3....."
    }

    println(fun3())

    //4. 有参数, 有返回值
    def fun4(name:String):String = {
      return s"fun4....$name"
    }

    println(fun4("lisi"))

    //5. 多个参数

    def fun5(name:String, age:Int, address:String ) :Unit = {
      println(s"name = $name , age = $age , address = $address")
    }

    fun5("wangwu",25,"beijing")
  }
}
