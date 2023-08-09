package com.atguigu.scala.chapter02

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.{ServerSocket, Socket}
import java.nio.charset.StandardCharsets

/**
  * 服务端
  */
object Scala07_server {
  def main(args: Array[String]): Unit = {
    //1. 创建serverSocket对象
    val server = new ServerSocket(9999)
    //2. 等待客户端的连接(阻塞)
    while(true){
      val client: Socket = server.accept()
      //起线程处理当前客户端的连接
      new Thread(
        new Runnable {
          //实现run方法
          override def run(): Unit = {
            //3. 通信
            //in
            val in =
            new BufferedReader(
              new InputStreamReader(
                client.getInputStream , StandardCharsets.UTF_8 ))
            val message: String = in.readLine()
            println("客户端说: " + message)

            //out
            val out =
              new PrintWriter( new OutputStreamWriter(client.getOutputStream ,StandardCharsets.UTF_8),true)

            out.println("Hello,client")


            out.close()
            in.close()
          }
        }
      ).start()
    }




    //client.close()
    //server.close()
  }
}
