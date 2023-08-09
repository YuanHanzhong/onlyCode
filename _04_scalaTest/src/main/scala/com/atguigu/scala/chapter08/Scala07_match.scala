package com.atguigu.scala.chapter08

/**
  * Scala - 模式匹配  - 偏函数
  */
object Scala07_match {
  def main(args: Array[String]): Unit = {

    // 将该List(1,2,3,4,5,6,"test")中的Int类型的元素加一
    val list = List(1,2,3,4,5,6,"test")

    //常规写法:
    println(
      list.map(num => {
      if (num.isInstanceOf[Int]) {
        num.asInstanceOf[Int] + 1
      } else {
        num
      }
    }))

    //模式匹配
    val result: List[Any] = list.map {
      case num: Int => num + 1
      case other => other
    }
    println(result)


    //偏函数
    //两个泛型: 1 -> 入参类型  2 -> 返回类型
    val pf: PartialFunction[Any, Any] = {
      case num : Int => num + 1
      case other  => other
    }

    println(list.map(pf))

    println("------------------------------------")

    // 将该List(1,2,3,4,5,6,"test")中的Int类型的元素加一，去掉其他类型的元素

//    println(
//      list.map{
//        case num :Int => num + 1
//      }
//    )

    println(
      list.collect{
        case num : Int => num + 1
      }
    )
  }

}
