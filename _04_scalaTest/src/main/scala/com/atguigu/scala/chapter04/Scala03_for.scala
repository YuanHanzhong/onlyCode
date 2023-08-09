package com.atguigu.scala.chapter04

/**
  * Scala循环控制 - for
  */
object Scala03_for {
  def main(args: Array[String]): Unit = {
    //1. 循环守卫
    //需求: 从1循环到5,当循环到3的时候跳过本次循环
    for( i <- 1 to 5 ){
//      if( i == 3){
//        continue   // scala中没有continue关键字
//      }
      if( i != 3 ){
        println(i)
      }
    }
    println("-------------------------")

    for( i <- 1 to 5  if i != 3 ){
        println(i)
    }

    println("-------------------------")

    // 引入变量
    //需求: 从1 循环到 5 ,打印每次循环变量的值 以及 循环变量+1的值

    for( i <- 1 to 5 ){
      var j = i + 1
      println("i = " + i + " , j = " + j )
    }

    println("-------------------------")

    for( i <- 1 to 5 ; j = i + 1 ){
      //var j = i + 1
      println("i = " + i + " , j = " + j )
    }

    println("-------------------------")

    //for循环的返回值
    // for循环表达式的返回值默认是 Unit ，如果希望将每次循环的结果返回，需要使用yield关键字.
    var result = for( i <- 1 to 5 ) yield {
      i
    }
    println(result)

    val list = List(1,2,3,4,5)
    var result1 =  for (elem <- list) yield  {
      elem + 1
    }

    println(result1)

    println("-------------------------")

    //循环嵌套
    // 外层循环执行一次,内存循环执行一轮
    for(i <- 1 to 5 ){
      //外层循环体
      for( j <- 1 to 3 ){
      //内层循环体
        println(s" i = $i , j = $j")
      }
    }

    println("-------------------------")

    for(i <- 1 to 5 ; j <- 1 to 3  ){
        //内层循环体
        println(s" i = $i , j = $j")
    }


    println("-------------------------")

    /*
        *
       ***
      *****
     *******
    *********
   ***********
  *************
 ***************
*****************

     */

    println( "*" * 3 )

  }
}
