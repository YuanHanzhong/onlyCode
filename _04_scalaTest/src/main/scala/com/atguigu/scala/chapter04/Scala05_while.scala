package com.atguigu.scala.chapter04

/**
  * Scala循环控制
  */
object Scala05_while {
  def main(args: Array[String]): Unit = {

    /*
      while(){} :  先判断条件再执行循环体


      do{}while() :  先执行循环体后判断条件



     */

    var num : Int = 30
    while(num <= 20){

      println("num  = " + num )
      num +=1
    }

    println("----------------------")

    do{
      println("num = " + num)
      num +=1
    }while(num <=20)
  }
}
