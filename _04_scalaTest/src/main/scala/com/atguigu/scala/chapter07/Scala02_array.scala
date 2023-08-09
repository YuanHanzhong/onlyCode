package com.atguigu.scala.chapter07

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Scala - 集合 - 数组
  */
object Scala02_array {
  def main(args: Array[String]): Unit = {
    // 可变数组  ArrayBuffer

    //1. 创建数组对象
    // 1.1 new
    //val arrayBuffer = new ArrayBuffer[Int]()

    // 1.2 apply
    val arrayBuffer: ArrayBuffer[Int] = ArrayBuffer(1,2,3,4,5)


    //2. 操作
    arrayBuffer.append(6,7)
    println(arrayBuffer.mkString(" , "))

    arrayBuffer.update(0,11)
    println(arrayBuffer.mkString(" , "))

    arrayBuffer.remove(0)
    println(arrayBuffer.mkString(" , "))

    arrayBuffer.remove(2,3)
    println(arrayBuffer.mkString(" , "))


    //3. 可变数组 与 不可变数组的转换

    val array = Array(1,2,3)

    val buffer: ArrayBuffer[Int] = ArrayBuffer(4,5,6)

    // 不可变 -> 可变
    val arrTobuu: mutable.Buffer[Int] = array.toBuffer

    // 可变 -> 不可变
    val buuToArr: Array[Int] = buffer.toArray

  }
}
