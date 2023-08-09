package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 递归
  */
object Scala14_function {
  def main(args: Array[String]): Unit = {
    /*
      递归: 自己调用自己.
      注意:
         1. 递归必须有退出条件
         2. 递归深度
     */

    //计算给定数字的累加和:
    //例如:  5  =>  5 + 4 + 3 + 2 + 1

    // sum(5) => 5 + sum(4)
    // sum(4) => 4 + sum(3)
    // sum(3) => 3 + sum(2)
    // sum(2) => 2 + sum(1)
    // sum(1) => 1

    def sum(num : Int ) : Int = {
      if( num == 1 ) {
        1
      }else {
        num + sum(num - 1 )
      }

    }

    println(sum(5))

    //尾递归:
    // sum1( 5, 1 )  => sum1(4, 5 + 1 )
    // sum1( 4, 5 + 1  )  => sum1(3 ,4 + 5 + 1  )
    // sum1( 3, 4 + 5 + 1 ) => sum1(2, 3 + 4 + 5 + 1 )
    // sum1( 2, 2, 3 + 4 + 5 + 1 ) => sum1(1, 2 + 3 +4 +5 + 1 )
    // sum1(1, 2 + 3 +4 +5 + 1 ) => 2 + 3 +4 + 5 + 1
    def sum1(num :Int , result : Int ) : Int = {

      if( num  == 1 ) {
        result
      }else{

        sum1(num - 1, num + result  )
      }
    }

    println(sum1(500000,1))




    def test(): Unit ={

      test()

      println("-----")
    }

  }
}
