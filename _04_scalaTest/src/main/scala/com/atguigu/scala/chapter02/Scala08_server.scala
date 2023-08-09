package com.atguigu.scala.chapter02

import java.io.ObjectInputStream
import java.net.{ServerSocket, Socket}

/**
  * 序列化    :  将内存中的数据转换成字节序列 , 用于持久化（落盘）或者网络传输
  *
  * 反序列化  :  将磁盘中的或者网络中接收到的字节序列, 转换成内存中的对象
  */
object Scala08_server {

  def main(args: Array[String]): Unit = {

    val server = new ServerSocket(9999)

    val client: Socket = server.accept()

    val in = new ObjectInputStream( client.getInputStream )

    val user: User = in.readObject().asInstanceOf[User]

    println(user.getUsername())
  }
}
