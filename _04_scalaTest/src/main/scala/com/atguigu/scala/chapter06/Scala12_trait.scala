package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 -  特质
  */
object Scala12_trait {
  def main(args: Array[String]): Unit = {


  }
}

// 声明特质
trait Operator12{

  var name : String = _

  var sex : String

  def testabstractMethod(): Unit

  def testMethod(): Unit = {}

}

trait DB12 /*extends Operator12*/{

}


class  MySQL12 extends Operator12 with DB12 {
  override var sex: String = _


  override def testabstractMethod(): Unit = {}


}