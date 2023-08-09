package com.atguigu.scala.chapter10

/**
  * Scala - 隐式转换 - 隐式函数 - 功能扩展
  *
  * OCP
  */
object Scala02_transform {
  def main(args: Array[String]): Unit = {
    val mySQL02 = new MySQL02 /* with Operator02 */

    mySQL02.insert()

    implicit  def transform( mySQL02: MySQL02 ) : MySQLExt02= {
        new MySQLExt02
    }

    mySQL02.select()  // MySQL02 -> MySQLExt02


    println("-----------------------------")


    var str : String = "abc"

    println(str.substring(0, 1))

    println(str.slice(0, 1))  // str -> StringOps

  }
}

//功能扩展类
class MySQLExt02{
  def select(): Unit = {
    println("查询数据....")
  }
}


trait Operator02{
  def select(): Unit = {
    println("查询数据....")
  }
}


class MySQL02 /*extends Operator02*/ {

  def insert(): Unit ={
    println("插入数据....")
  }

  /*
  def select(): Unit = {
    println("查询数据....")
  }
   */

}
