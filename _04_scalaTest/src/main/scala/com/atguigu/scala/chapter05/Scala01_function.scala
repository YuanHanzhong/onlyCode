package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 基本语法
  */
object Scala01_function {
  //方法
  def main(args: Array[String]): Unit = {
    /*
      函数语法:
        [访问修饰符] def  函数名( 函数参数列表  ) : 返回值类型 = { 函数体 }


      函数 & 方法:
        函数: 函数式编程中的概念
        方法: 面向对象编程中的概念

      区分 函数 &方法:
         1. 方法其实就是函数.
         2. 从定义的位置进行区分. 定义到类中的称之为方法。其他位置的称之为函数.
         3. 方法是面向对象的概念，因此方法有面向对象的特征,方法可以重写或者重载.
         4. 函数式函数式编程的概念， 函数不能重写或者重载. 但是函数拥有其他强大的
            的功能, 函数可以嵌套声明, 函数可以作为值、作为参数、作为返回值等来使用.
     */


    //函数
    def testFun():Unit = {
        //函数
        def testInnerFun() : Unit = {

        }
    }

    /*

    def testFun(name:String) :Unit = {

    }

     */

  }

  //方法

  def testMethod():Unit = {

  }

  def testMethod(name :String) :Unit = {

  }
}
