package com.atguigu.scala.chapter10

/**
  * Scala - 隐式转换 - 隐式规则的查找范围
  */
object Scala05_transform  extends Scala06_transform {
  def main(args: Array[String]): Unit = {
    /*
    implicit def transform(num : Double  ) : Int = {
       println("当前代码作用域....")
       num.toInt
    }
     */
    var num : Int = 2.0D

    val mysql05 = new MySQL05

    mysql05.select()


  }

  /*
  implicit  def transform(num : Double) :Int = {
    println("上级作用域....")
    num.toInt
  }

   */

  implicit  class MySQLExt05(mySQL05: MySQL05) {

    def select(): Unit ={
      println("查询...上级作用域 ")
    }
  }
}



class MySQL05{

  def insert(): Unit ={
    println("插入....")
  }
}
