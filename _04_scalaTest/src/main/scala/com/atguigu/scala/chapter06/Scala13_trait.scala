package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 -  特质
  */
object Scala13_trait {
  def main(args: Array[String]): Unit = {

      //动态混入
      val mySQL13 = new MySQL13 with Operator13 with DB13{
        override def operData(): Unit = {

        }

        override def selectData(): Unit = {

        }
      }

      mySQL13.operData()
      mySQL13.selectData()
  }
}

// 声明特质
trait Operator13{

  def  operData(): Unit
}

trait DB13 {

  def selectData(): Unit
}


// 静态混入
/*
class  MySQL13 extends Operator13 with DB13 {

  override def operData(): Unit = {
     println("MySQL13 ... operData")
  }

  override def selectData(): Unit = {
    println("MySQL13 ... selectData")
  }
}*/


class MySQL13{

}
