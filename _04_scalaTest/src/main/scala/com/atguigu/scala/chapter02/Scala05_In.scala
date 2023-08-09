package com.atguigu.scala.chapter02

import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}
import java.nio.charset.StandardCharsets

import scala.io.{BufferedSource, Source, StdIn}

/**
  * Scala输入
  */
object Scala05_In {
  def main(args: Array[String]): Unit = {

    //1. 从控制台输入
    // Java : new Scannner(System.in)
    //println("请输入您的名字:")
    //val line: String = StdIn.readLine()
    //println(line)


    //2. 从文件中输入
    //2.1 使用java中的io流
    // 输入流:  FileInputStream InputStreamReader BufferedReader .....

    val fis = new FileInputStream( new File("input/word.txt"))
    val isr = new InputStreamReader(fis,StandardCharsets.UTF_8)
    val br = new BufferedReader(isr)

    //var line = ""
    /*
         Scala中的表达式都是有值的。

         (line = br.readLine())!= null
            Java  :  (line = br.readLine())!= null 判断line是否为null
            Scala :  (line = br.readLine())!= null 判断 (line = br.readLine()) 是否为null
     */
    //while((line = br.readLine())!= null ){
      //println(line) // line == null
    //}

    var flag = true
    while(flag){
      var line = br.readLine() ;
      if(line == null ){
        flag = false
      }else{
        println(line)
      }
    }

    br.close()

    println("------------------------------------------")


    //2.2 Source

    val strings: Iterator[String] = Source.fromFile("input/word.txt").getLines()
    while(strings.hasNext){
      val line: String = strings.next()
      println(line)
    }

    //如下的写法先了解

    println("------------------------------------------")

    Source.fromFile("input/word.txt").getLines().foreach(println(_))

    println("------------------------------------------")

    Source.fromFile("input/word.txt").getLines().foreach(println)


  }
}
