package com.atguigu.scala.chapter02

import java.util

/**
  * Scala 数据类型
  */
object Scala09_datatype {
  def main(args: Array[String]): Unit = {
    // AnyVal
    var b : Byte = 1
    var s : Short = 1
    var i : Int = 1
    var boo : Boolean = false

    // AnyRef
    var name : String  = "zhangsan"
    //java集合类
    val arrayList = new util.ArrayList[String]
    //Scala集合类
    val list = List("a","b","c","d",1,2,4,false )

    // 类型转换
    // AnyVal - 自动类型转换

    var bb : Byte = 1
    var ss : Short = bb  //BoxesRunTime.boxToShort(bb)
    println(ss)

    //AnyVal - 强制类型转换
    // 在Scala中的Byte、 Short、 Char 、 Int、 Long、 Float 、Double中有提供了统一的转换方法:
    // toByte toShort toChar  toInt  toLong  toFloat  toDouble

    var l : Long = 123L
    var ii : Int = l.toInt


    //AnyRef - 自动类型转换

    var username : String = "lisi"

    var af : AnyRef  = username

    var any : Any = af

    //AnyRef - 强制类型转换
    // asInstanceOf[类型]
    // isInstanceOf[类型]
    val anyRefUser : AnyRef  = new User()
    if( anyRefUser.isInstanceOf[User]){
      val user: User = anyRefUser.asInstanceOf[User]
    }


    //字符串类型转换
    // Scala中任何类型都可以直接调用toString方法转换成字符串类型

    var f: Float = 1.0f

    println(f.toString)

    val user = new User()
    println(user.toString)


  }
}
