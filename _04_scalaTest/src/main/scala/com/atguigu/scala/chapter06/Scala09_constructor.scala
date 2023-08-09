package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 - 构造方法
  */
object Scala09_constructor {
  def main(args: Array[String]): Unit = {
    /*
        Java的构造器:
          1. 默认构造器
             [修饰符] 类名(){ 构造体 }

          2. 构造器支持重载

          3. 子类一定要调用父类构造器

        Scala的构造器:

          1. 主构造器
               Scala将类体和主构造整合到一起. 类体就是构造体

          2. 辅助构造器
               def this(参数列表 ){ 构造体 }

               辅助构造器必须直接或间接调用主构造器
               被调用的辅助构造器一定要声明到调用者的前面.





     */

    val user09 = new User09()

    val user099 = new User09("lisi")


  }
}

class User09 /* private */ (username:String )   {

  println("User09.....")


  def this(){
    this("zhangsan")
    println("this()")
  }

  def this(username:String , age : Int ) {
    this()
  }


}
