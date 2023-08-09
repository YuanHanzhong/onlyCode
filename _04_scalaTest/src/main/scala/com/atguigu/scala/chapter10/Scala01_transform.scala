package com.atguigu.scala.chapter10

/**
  * Scala - 隐式转换 - 隐式函数
  */
object Scala01_transform {
  def main(args: Array[String]): Unit = {
    //数据类型 - 隐式转换
    var b : Byte  = 10
    var i : Int = b   //BoxesRunTime.boxToInteger(b)
    println(i)

    println("------------------------")


    //var num : Int = 2.0D.toInt

    //var num : Int = transform(2.0D)

//    def transform(d: Double) : Int = {
//      d.toInt
//    }

    implicit def doubleToInt( d : Double ) : Int = {
      d.toInt
    }


    var num : Int = 2.0D  // 编译报错  -> 查找隐式转换规则（自定义）  -> 再次进行编译  -> 通过、不通过

    println(num)



  }
}
