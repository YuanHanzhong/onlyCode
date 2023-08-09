package com.atguigu.scala.chapter07

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Scala - 集合 - List
  */
object  Scala03_list {
  def main(args: Array[String]): Unit = {
    // 不可变 List
    // 1. 创建对象
    val list = List(1,2,3,4,5)
    println(list)
    println(list.getClass.getName)

    // 可变 ListBuffer
    //val listBuffer = new ListBuffer[Int]()
    val listBuffer: ListBuffer[Int] = ListBuffer(1,2,3,4,5)

    listBuffer.append(6,7)
    println(listBuffer)

    listBuffer.remove(0)
    println(listBuffer)

    // 符号操作
    // ::
    val nil: Nil.type = Nil
    println(nil)

    val list1: List[Int] = 1 :: 2 :: 3 :: Nil
    println(list1)

    // :::

    val list2 = List(5,6,7)

    val list3: List[Any] = 1::2::3::list2::Nil
    println(list3)

    val list4: List[Any] = 1::2::3::list2:::Nil
    println(list4)



    //可变List  和 不可变List的转换

    val list5 = List(1,2,4)

    val listBuffer2: ListBuffer[Int] = ListBuffer(7,8,9)

    // 不可变 -> 可变
    val liiToBuu: mutable.Buffer[Int] = list5.toBuffer

    //可变 -> 不可变
    val buuToLii: List[Int] = listBuffer2.toList

  }
}
