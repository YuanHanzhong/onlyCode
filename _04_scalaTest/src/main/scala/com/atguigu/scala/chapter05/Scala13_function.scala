package com.atguigu.scala.chapter05

import scala.util.control.Breaks

/**
  * Scala - 函数式编程 - 控制抽象
  */
object Scala13_function {

  def main(args: Array[String]): Unit = {
    // 控制抽象就是支持将一段代码作为参数进行传递.
    Breaks.breakable{

      for( i <- 1 to 5 ) {
        if( i == 3 ) {
          Breaks.break()
        }
        println(" i = " + i )
      }

    }

    println("------------")

    def fun( op : => Unit  ) {
      println("----------begin------------")

      op

      println("----------end------------")
    }

    //fun( println("haha") )

    fun {
      var i = 10
      i += 1
      println(" i = " + i)
    }


  }
}
