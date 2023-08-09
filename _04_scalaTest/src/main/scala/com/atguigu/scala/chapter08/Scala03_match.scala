package com.atguigu.scala.chapter08

/**
  * Scala - 模式匹配
  */
object Scala03_match {
  def main(args: Array[String]): Unit = {
    //匹配对象:  对象的匹配还是比较对象的内容。

    val user03 = new User03("lisi",30)

    val result : String = user03 match {
      case User03("zhangsan", 30) => "ok"
      case _ => "no"
    }
    println(result)
  }
}

class User03(var username : String , var age : Int ){

  //var username : String = _

  //var age : Int = _

}

object User03{

  //对象的匹配会自动调用unapply方法
  def unapply(user: User03): Option[(String, Int )] = {
    if (user == null)
      None
    else
      Some(user.username, user.age)
  }
}