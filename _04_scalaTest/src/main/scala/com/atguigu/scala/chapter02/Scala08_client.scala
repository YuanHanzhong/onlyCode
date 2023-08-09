package com.atguigu.scala.chapter02

import java.io.ObjectOutputStream
import java.net.Socket

object Scala08_client {
  def main(args: Array[String]): Unit = {
    val socket = new Socket("localhost",9999)

    val user = new User()

    //java.io.NotSerializableException: com.atguigu.scala.chapter02.User

    val out = new ObjectOutputStream(socket.getOutputStream)

    out.writeObject(user)

  }
}
