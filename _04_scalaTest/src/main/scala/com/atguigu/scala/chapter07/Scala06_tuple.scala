package com.atguigu.scala.chapter07

/**
  * Scala - 集合  - 元组
  */
object Scala06_tuple {
  def main(args: Array[String]): Unit = {

    // "zhangsan"   30   false

    val tuple: (String, Int, Boolean) = ("zhangsan", 30 , false )

    val name: Any = tuple.productElement(0)
    println(name)
    if(name.isInstanceOf[String]){
      val namestr: String = name.asInstanceOf[String]
    }

    val username: String = tuple._1

    val age: Int = tuple._2

    val flag: Boolean = tuple._3


    //Map 和 tuple的关系
    // k -> v  == ( k,v )
    // Map中的kv本质上就是一个两个元素的tuple对象， 称之为对偶元组.
    val map: Map[String, Int] = Map( "a"->1 , "b" ->2 , "c"->3 , ("d",4) )
    println(map)

    for (elem <- map) {
      //println(elem)
      println(elem._1  + " : " + elem._2)
    }

  }
}
