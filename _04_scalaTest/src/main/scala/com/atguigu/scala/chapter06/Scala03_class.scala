package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 - 类
  */
object Scala03_class {
  /*
     Java的类:
       1. 声明语法:
            [访问权限修饰符]  class  类名 { 类体 }

       2. 一个java文件中的多个类:
            只能有一个public修饰,且public修饰的类的名字必须跟文件名一致.


     Scala的类:
       1. 声明语法:
           [访问权限修饰符]  class 类名 { 类体 }

       2. 一个scala文件中的多个类:
            没有限制public修饰的类的个数

       3. object 和 class:

          class修饰的类我们称之为伴生类
          object修饰的类我们称之为伴生对象.


   */

  def main(args: Array[String]): Unit = {

    //User03.testobjectUser03()

    val user03 = new User03
    user03.testclassUser03()
  }


}


/*public*/ class User03{

  def testclassUser03(): Unit ={

  }
}

//object User03{
//
//  def testobjectUser03(): Unit ={
//
//  }
//
//}

/*public*/ class User033{}

/*public*/ class User0333{}