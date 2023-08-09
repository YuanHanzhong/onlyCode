package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 - 构造方法
  */
object Scala09_constructor_1 {
  def main(args: Array[String]): Unit = {
    //主构造器参数通过var或者val声明， 参数即为属性.
    val user09_1 = new User09_1("zhangsan",30)

    println(user09_1.username)
    user09_1.username = "lisi"
    println(user09_1.userage)
    //user09_1.userage = 40

  }
}


class User09_1 (var username : String , val userage : Int ) {

}

/*
class User09_1 (name : String , age : Int ) {

  var username : String = name

  var userage : Int = age
}

 */
