package com.atguigu.scala.chapter10

/**
  * Scala - 隐式转换 - 隐式类
  */
object Scala03_transform {
  def main(args: Array[String]): Unit = {
    val mySQL03 = new MySQL03

    mySQL03.insert()

    /*
    //隐式规则
    implicit  def transform(mySQL03: MySQL03): MySQLExt03 ={
        new MySQLExt03
    }

     */

    // 隐式类 =  功能扩展类 + 隐式规则
    implicit  class MySQLExt03(mySQL03: MySQL03){
      def select(): Unit ={
        println("查询数据....")
      }
    }

    mySQL03.select()
  }
}





/*
//功能扩展类
class MySQLExt03{
  def select(): Unit ={
    println("查询数据....")
  }
}
*/

class MySQL03{

  def insert(): Unit ={
    println("插入数据....")
  }
}
