package com.atguigu.scala.chapter02

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter, PrintWriter}
import java.nio.charset.StandardCharsets

import scala.io.StdIn

/**
  * Scala输出
  */
object Scala06_out {

  def main(args: Array[String]): Unit = {
    println("请输入内容: ")
    val line: String = StdIn.readLine()

    //将接收到的内容写入到 output/out.txt
    //输出流 ： FileOutputStream  OutputStreamWriter  BufferedWriter  PrintWriter ....

    val fos = new FileOutputStream(new File("output/out.txt"))
    val osw = new OutputStreamWriter(fos,StandardCharsets.UTF_8)
    //val bw = new BufferedWriter(osw)
    val pw = new PrintWriter(osw,true)

    pw.println(line)
    pw.close();


  }
}
