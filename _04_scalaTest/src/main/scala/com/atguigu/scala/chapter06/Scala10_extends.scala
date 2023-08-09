package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 - 继承
  */
object Scala10_extends {
  def main(args: Array[String]): Unit = {
    /*
      Java中的继承:

        1. 单继承, extends

        2. 子类一定要调用父类的构造器

        3. 方法的重写

      Scala中的继承:
        1. 单继承, extends

        2. 子类一定要调用父类的构造器

        3. 方法的重写

     */

    val user10: Parent10 = new User10("zhangsan")

    user10.test()

  }
}

class Parent10( pname : String ){

  def test():Unit = {
    println("test... parent10")
  }

}

class User10 (uname : String ) extends Parent10(uname) {

  override def test(): Unit = {
    println("test... user10")
  }
}
