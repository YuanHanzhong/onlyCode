package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 柯里化
  */
object Scala12_function {
  def main(args: Array[String]): Unit = {
    // 函数柯里化就是函数支持多个参数列表

    //1. 一个普通函数
    def regist(username:String ,password : String = "000000" , address:String ):Unit = {
      println(s"username = $username , password = $password , address = $address")
    }

    regist("zhangsan","123123","beijing")

    //2. 柯里化

    def regist1(username:String) (password : String = "111111" ) (address:String) : Unit = {
      println(s"username = $username , password = $password , address = $address")
    }

    regist1("lisi")("000000")("shanghai")


    //参数默认值

    //隐式转换  implicit
    val list = List(4,2,1,5,3)

    println(list.sortBy(num => num)(Ordering.Int.reverse)  )




  }
}
