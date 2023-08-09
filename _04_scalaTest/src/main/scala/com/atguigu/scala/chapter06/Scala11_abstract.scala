package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 - 抽象
  */
object Scala11_abstract {

  def main(args: Array[String]): Unit = {

    /*
        抽象: 不完整,不具体.

        Java中的抽象:
           1. 抽象类

           2. 抽象方法

           3. 一个类中包含抽象方法，该类一定是抽象类.
              一个抽象类中不一定包含抽象方法.

        Scala中的抽象:

           1. 抽象类

           2. 抽象方法

           3. 抽象属性

           4. 一个类中包含抽象方法，该类一定是抽象类.
              一个抽象类中不一定包含抽象方法.

           5. 一个具体类继承抽象类后,需要将抽象类中的抽象方法补充完整.
              如果重写抽象方法, override关键字可加可不加.
              如果重写具体方法, override关键字必须加

           6. 一个具体类继承抽象类后,需要将抽象类中的抽象属性补充完整.
              如果重写抽象属性, override关键字可加可不加.
              如果重写具体属性, override关键字必须加

              val声明的属性才能被重写.


     */
  }
}


abstract  class Parent11{

  //声明一个抽象属性 (本质上是抽象方法)
  var name : String

  //声明具体属性
  var age : Int = 30

  val address: String = "beijing"



  //声明一个抽象方法
  def testabstractMethod() : Unit

  //声明具体方法
  def testMethod(): Unit = { println( " Parent11 ... testMethod" ) }


}



class User11 extends Parent11{

  //补全抽象属性
  override var name: String = _

  //重写具体属性
  //override var age : Int = 40
  override val address: String = "shanghai"



  //补全抽象方法
  override def testabstractMethod(): Unit = {
    println("User11 ... testabstractMethod")
  }

  //重写具体方法
  override def testMethod(): Unit = {
    println("User11 ... testMethod")
  }

}