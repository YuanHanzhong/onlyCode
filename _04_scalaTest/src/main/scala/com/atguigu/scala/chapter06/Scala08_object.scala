package com.atguigu.scala.chapter06

/**
  *  Scala - 面向对象编程  -  对象
  */
object Scala08_object {
  def main(args: Array[String]): Unit = {

    /*
      创建对象的方式:
        1.  new
        2.  反射
        3.  clone
        4.  反序列化


      scala提供的一种创建对象的方式： apply方法
      apply方法声明到伴生对象中，用于创建伴生类的对象.
      scala能自动识别apply方法



     */


      //val user08 = User08
      //println(user08.getClass.getName)  //com.atguigu.scala.chapter06.User08$

    //val user1: User08 = User08.apply()
      val user1: User08 = User08()
      println(user1)

      val user2: User08 = User08.abc()


      //val user08 = new User08  // 因为构造器私有化

  }
}

class User08 private {

}

object User08{

  def apply() :  User08 = {
    new User08()
  }

  def abc() :  User08 = {
    new User08()
  }
}
