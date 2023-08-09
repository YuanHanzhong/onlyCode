package com.atguigu.scala.chapter08

/**
  * Scala - 模式匹配
  */
object Scala04_match {
  def main(args: Array[String]): Unit = {

    //val user04 = new User04("zhangsan",30)
    val user04 = User04("zhangsan",30)

    //user04.username = "lisi"

    user04.age

    val result: String = user04 match {

      case User04("zhangsan", 30) => "ok"
      case _ => "no"
    }

    println(result)

  }
}
//样例类
//样例类是专门为了模式匹配优化的类.
//样例类会自动提供 apply 、unapply 、.....等方法
//样例类的主构造函数中的参数默认使用val声明.  如果需要修饰，手动添加var进行修饰.
//样例类默认实现了序列化接口.

//case class User044() {}

case class User04(username : String, age : Int ){}

/*
object User04{

  def unapply(user: User04): Option[(String, Int )] = {
    if (user == null)
      None
    else
      Some(user.username, user.age)
  }
}
 */