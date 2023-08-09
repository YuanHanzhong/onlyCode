package com.atguigu.scala.chapter01

/**
  *  1. Java是面向对象的语言.
  *     Scala是完全面向对象的语言
  *
  *  2. object ：因为Scala是完全面向对象的语言，因此java中的很多非面向对象的语法在Scala中不存在.
  *              例如: static语法，
  *              object用来模拟static语法。
  *
  *  3. def  ： 定义/声明方法或者函数.
  *
  *  4. 方法(函数)形参:
  *         Java是强类型语言.Scala是强类型语言.
  *         Java  :  String[] args        =>   参数类型 参数名     => 语法风格  =>  我爱上了一个女人,她叫小花
  *         Scala :  args: Array[String]  =>   参数名 : 参数类型   => 语法风格  =>  我爱上了小花， 她是女人
  *         Person  xiaohua  = new Person()
  *         xiaohua.chifan();
  *         xiaohua.getName();
  *         xiaohua.shuijiao();
  *
  *  5. Array[String] :
  *         Array: 数组类型
  *         [String]： []表示泛型。
  *
  *  6. 方法（函数）签名:
  *        Java  ：   void main()  =>  返回值类型 方法名
  *        Scala :    main(): Unit =>  方法名 : 返回值类型
  *
  *  7. 无返回值:
  *        Java  : void  => 关键字
  *        Scala : Unit  =>  无返回值类型   => ()
  *
  *  8. = : 赋值运算.  Scala是完全面向对象的语言，因此方法(函数)也是对象，可以被赋值.
  *
  *  9. println() : 等价于 Predef.println("Hello Scala")
  *                 在Scala中会默认导入Predef中的内容
  *
  *  10. 语句结束的分号:
  *          分号表示语句的结束， 在Scala中，没有歧义的情况下分号可以写也可以不写,一般会省略不写.

  */

object Scala01_HelloWorld {
  //程序的入口

  def main(args: Array[String]): Unit = {

    System.out.println("Hello Scala");

    println("Hello Scala")

    Predef.println("Hello Scala")
  }

}
