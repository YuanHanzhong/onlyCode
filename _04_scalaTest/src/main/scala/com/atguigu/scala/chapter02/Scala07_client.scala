package com.atguigu.scala.chapter02

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.Socket
import java.nio.charset.StandardCharsets

/**
  * 客户端
  */
object Scala07_client {
  def main(args: Array[String]): Unit = {
    //1.创建客户端对象
    val client = new Socket("localhost",9999)

    //2. out
    val out =
      new PrintWriter(
        new OutputStreamWriter(
          client.getOutputStream,StandardCharsets.UTF_8 ),true)

    out.println("Hello,Server")


    //3. In

    val in =
      new BufferedReader(
        new InputStreamReader(
          client.getInputStream , StandardCharsets.UTF_8 ))
    val message: String = in.readLine()

    println("服务端说: " + message)

    out.close()
    in.close()
    //client.close()

  }
}
