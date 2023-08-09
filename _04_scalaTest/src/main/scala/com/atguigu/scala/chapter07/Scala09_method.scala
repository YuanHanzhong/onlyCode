package com.atguigu.scala.chapter07

/**
  * Scala  - 集合 - 常用方法
  */
object Scala09_method {
  def main(args: Array[String]): Unit = {
    val list = List(1,2,3,4,5)

    //27. 集合中元素的最小值
    println(list.min)

    //28. 集合中元素的最大值
    println(list.max)

    //29. 集合中元素求和
    println(list.sum)

    //30. 集合中元素乘积
    println(list.product)

    //31. 集合规约
    //    实现集合中元素的汇总操作
    println(list.reduce(_ + _))

    //32. 集合左规约
    println(list.reduceLeft(_ + _))

    //33. 右规约
    println(list.reduceRight(_ + _))

    println("--------------------------")


    val list1 = List(1,2,3,4,5)

    println(list1.reduceLeft(_ - _))  // -8  -13

    println(list1.reduceRight( _ - _ ))  // -2  3


    //34. 集合折叠

    println(list.fold(6)(_ + _))

    //35. 集合左折叠
    println(list.foldLeft(6)(_ + _))

    //36. 集合右折叠
    println(list.foldRight(6)(_ + _))


    println("--------------------------")

    println(list.foldLeft(6)(_ - _))  // -9

    println(list.foldRight(6)(_ - _))  // -3


    //37. 集合扫描
    println(list.scan(6)(_ + _))

    //38. 集合左扫描
    println(list.scanLeft(6)(_ + _))

    //39. 集合右扫描
    println(list.scanRight(6)(_ + _))





  }
}
