package com.atguigu.scala.chapter02

/**
  * Scala变量
  */
object Scala02_variable {
  def main(args: Array[String]): Unit = {
    //1. 声明变量的语法:
    //   var | val  变量名 : 变量类型 = 变量值
    //var username : String  = "zhangsan"
    //var age : Int  = 25

    //2. 类型推断 ,定义变量的时候如果Scala能通过变量值自动将类型进行推断，我们可以省略类型

    var username   = "zhangsan"

    var age = 30

    var flag  = false


    //3. 变量的初始化
    /*
        Java中变量的初始化:
           1)  int i  ;  先声明
               i = 10 ;  后初始化

           2) int i = 10 ; 声明的同时初始化

        Scala中变量的初始化:
           1) 声明的同时初始化

     */

    var name : String = "lisi"

    //4. 可变变量var   和 不可变变量val

    var address : String  = "beijing"
    address = "shanghai"
    println(address)
    val sex : String  = "男"
    //sex = "女"
    println(sex)


  }
}
