package com.atguigu.scala.chapter04

import scala.util.control.Breaks._  //导入Breaks对象的内容

/**
  * Scala循环控制
  */
object Scala04_for {
  def main(args: Array[String]): Unit = {
    // 循环中断

    //需求: 从1 循环到5 ,当循环到3的时候退出循环

    for( i <- 1 to 5 ){
//      if( i == 3){
//        break   // Scala中没有break关键字
//      }
      println(" i = " + i )
    }

    println("-------------------------------")



    breakable {
      for (i <- 1 to 5) {
        if (i == 3) {
          // 退出循环
          break
        }
        println(" i = " + i)
      }

    }

    println("我咋办???")




  }
}
