package com.atguigu.scala.chapter02

class User extends Serializable   {  // 混入特质
  var username : String = "zhangsan"

  def getUsername(): String = {
    return username
  }

  override def toString: String = {
    return s"[User: username = $username]"
  }

}
