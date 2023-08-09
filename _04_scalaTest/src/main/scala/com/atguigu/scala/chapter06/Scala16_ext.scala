package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 - 扩展
  */
object Scala16_ext {
  def main(args: Array[String]): Unit = {
    //1. isInstanceOf[类型]
    //   asInstanceOf[类型]

    //2. 获取Class对象

    /*
        Java中获取Class对象
          1. Class.forName("全类名")

          2. obj.getClass()

          3. 类名.class

        Scala中获取Class对象

          1. Class.forName("全类名")

          2. obj.getClass()

          3. classOf[类型]

     */

    val c1: Class[_] = Class.forName("com.atguigu.scala.chapter06.TestClass")
    println(c1)

    val testClass = new TestClass
    val c2: Class[_ <: TestClass] = testClass.getClass
    println(c2)

    val c3: Class[TestClass] = classOf[TestClass]
    println(c3)


    println(Color.RED)

    println(Color.RED.id)


    //type定义新类型(取别名)

    type newTestClass   = com.atguigu.scala.chapter06.TestClass





  }
}

class TestClass{}



// 枚举类
object Color extends Enumeration {
  val RED = Value(1, "red")
  val YELLOW = Value(2, "yellow")
  val BLUE = Value(3, "blue")
}


// 应用类
object AppTest extends App {

  println("application");

}



