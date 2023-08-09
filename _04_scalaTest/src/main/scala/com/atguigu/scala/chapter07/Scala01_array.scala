package com.atguigu.scala.chapter07

import java.util

/**
  * Scala - 集合  - 数组
  */
object Scala01_array {
  def main(args: Array[String]): Unit = {
    //不可变数组  Array
    //1. 创建对象
    //1.1 new
    //val array = new Array(5)
    //val array = new Array[Int](5)   //int[] var2 = new int[5];

    //1.2 apply
    val array: Array[Int] = Array.apply[Int](1,2,3,4,5)
    //val array: Array[Object] = Array("a","b","c")

    //2. 操作
    array.update(0,11)
    println(array)
    // 循环打印   迭代器
    println(util.Arrays.toString(array))
    println(array.mkString(" , "))

    array(1) = 22
    println(array.mkString(" , "))
    println(array(1))

    //符号操作
    // :+
    //val array1: Array[Int] = array.:+(6)
    val array1: Array[Int] = array :+ 6
    println(array1.mkString(" , "))

    // +:
    // 以:结尾的方法是反向操作。
    //val array2: Array[Int] = array.+:(6)
    val array2: Array[Int] = 6 +: array
    println(array2.mkString(" , "))


    //3. 其他数组
    //多维数组
    val array3: Array[Array[Int]] = Array.ofDim[Int](3,3)

    for (elem <- array3) {
      println(elem.mkString(" , "))
    }

    //合并数组
    val array4 = Array(1,2,3)
    val array5 = Array(4,5,6)
    val array6: Array[Int] = Array.concat(array4,array5)
    println(array6.mkString(" , "))

    //创建指定范围的数组
    val array7: Array[Int] = Array.range(0,5,2)
    println(array7.mkString(" , "))

    //创建并填充指定数量的数组
    val array8: Array[Int] = Array.fill(5)(-1)
    println(array8.mkString(" , "))










  }
}
