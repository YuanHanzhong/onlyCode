package com.atguigu.scala.chapter09

import java.io.{File, FileInputStream, FileNotFoundException}

/**
  * Scala - 异常
  */
object Scala01_exception {
  def main(args: Array[String]): Unit = {
    // 语法:
    // try .... catch(1) ... finally

    // throw

    // throws 没有

    // @throws注解声明异常

    try{

      var a : Int = 20
      var b : Int = 0
      var c  = a / b

    }catch{
      //模式匹配
      case ex : ArithmeticException => println("算术异常: " + ex.getMessage)
      case ex : NullPointerException => println("空指针了....")
      case ex : Exception => throw new RuntimeException(ex.getMessage)

    }finally{
      println("finally")
    }

    new FileInputStream(new File("d:\\aaa"))


  }


  @throws[FileNotFoundException]
  def testException(): Unit ={
    throw new RuntimeException("hehe")
  }
}
