package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 闭包
  */
object Scala11_function {
  def main(args: Array[String]): Unit = {

    //闭包: 内部函数使用外部函数的局部变量.

    //Scala 2.12.11 ： 改变内部函数的参数列表,将要使用的外部函数的局部变量通过参数进行接收.
    //                 将外部函数的局部变量作为内部函数的参数传递进去， 在内部函数中就可以正常使用.


    //Scala 2.11.8 :  将外部函数中的局部变量提升成类的全局属性.

    def outFun(i : Int ): Int => Int = {

      def innerFun(j :Int) : Int = {
        i + j
      }

      innerFun
    }

    println(outFun(10)(20))

    outFun(10)(20)
  }
}
