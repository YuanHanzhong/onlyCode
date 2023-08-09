package com.atguigu.scala.chapter06

/**
  *  Scala  - 面向对象编程 -  方法
  */
object Scala07_method {
  /*
      Java中的方法:
        1. 声明语法
             [修饰符] 返回值类型  方法名( 参数列表 ) {  方法体 }

        2. 方法的特性：
             重载
             重写


      Scala中的方法:

        1. 声明语法:
            [修饰符] def  方法名( 参数列表 ) ： 返回值类型 = { 方法体 }
        2. 方法的特性
             重载
             重写

   */

  def main(args: Array[String]): Unit = {

    //var num : Byte  = 10
    //test(num)

    val user07 : User07 = new User07

    test(user07)

  }

  def test(p : Parent07) : Unit = { println("Parent07")}

  //def test(u : User07) : Unit = { println("User07")}

  def test(i : Int ) : Unit = { println("Int")}

  def test(c : Char) : Unit = { println("Char")}

  //def test(s : Short): Unit = { println("Short")}

  //def test(b : Byte) : Unit = { println("Byte")}
 }

class Parent07{

}
class User07 extends Parent07{

}



