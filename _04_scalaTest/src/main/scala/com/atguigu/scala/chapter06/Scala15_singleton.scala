package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 - 单例
  */
object Scala15_singleton {
  def main(args: Array[String]): Unit = {
    /*
        单例: 一个类只能创建一个对象.

        Java中的单例:
           1. 构造器私有化

           2. 提供获取对象的方法 (static)


        Scala中的单例:
            1. 构造器私有化

            2. 提供获取对象的方法


     */

    val u1 = User15()

    val u2 = User15()

    println(u1 eq u2)



  }
}
//构造器私有化
class User15 private{

}

//伴生对象: 单例
object User15{

  private val user = new User15

  def apply(): User15 = user

  /*
  var user : User15 = null

  def apply(): User15 = {
    if(user == null ){
      user = new User15
      user
    }else{
      user
    }
  }
  */


}
