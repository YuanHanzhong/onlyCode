package com.atguigu.scala.chapter04

/**
  * Scala循环控制 - for
  */
object Scala02_for {
  def main(args: Array[String]): Unit = {
    /*
      java的for:
        1. fori : 关心循环次数
           for( int i = 1 ; i <=100 ; i++){

           }

        2. fore : 关心遍历数据集中的元素

           for( String str : List<String> ){
           }


      scala的for:
          for(循环变量 <- 数据集) {}

     */

    // 实现从1 循环到 5
    for( i  <- 1 to 5 ){
      println(i)
    }

    // 实现将数据集中的元素进行遍历
    val list = List("a","b","c","haha")

    for (elem <- list) {
      println(elem)
    }

    println("-------------------------")
    for (elem <- 1 until 5) {
      println(elem)
    }

    println("-------------------------")

    for(i <- Range.inclusive(1,5)){
      println(i)
    }

    println("-------------------------")

    for(i <- Range(1,5)){
      println(i)
    }
    println("-------------------------")

    // 循环步长

    for( i <- 1 to 5 by 2 ){
      println(i)
    }

    println("-------------------------")

    for( i <- 1 until 5 by 2 ){
      println(i)
    }


    println("-------------------------")

    for( i <- Range.inclusive(1,5,2) ){
      println(i)
    }

    println("-------------------------")

    for( i <- Range(1,5,2) ){
      println(i)
    }




  }
}
