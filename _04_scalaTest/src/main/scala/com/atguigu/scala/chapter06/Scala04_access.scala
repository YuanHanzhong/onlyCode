package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 - 访问权限
  */
object Scala04_access {
  def main(args: Array[String]): Unit = {
    /*
       java的访问权限修饰符:
         private    :    本类
         [default]  :    本类    本包
         protected  :   本类    本包    子类
         public     :    任意

       Scala的访问权限修饰符:
         private      :  本类
         private[包]  ： 本类    本包
         protected    :  本类   子类
         [default]    ： 任意

     */

    val user04 = new User04

    // user04.password

    println(user04.age)
    println(user04.username)


  }
}

class User04{

  private var password : String = "nicai"

  private[chapter06] var age : Int  = 30

  protected var money : Int = 10

  var username : String = "atguigu"






  def testAccess(): Unit ={
    println(this.password)
    println(this.age)
    println(this.money)
    println(this.username)
  }

}

class SubUser04 extends User04{

  override def testAccess(): Unit = {

    println(this.money)
    println(this.username)

  }

}
