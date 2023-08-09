package com.atguigu.scala.chapter10

/**
  * Scala - 隐式转换 - 隐式参数 - 隐式变量
  */
object Scala04_transform {
  def main(args: Array[String]): Unit = {

    def regist(username : String, password : String ) = {
      println(s"username = $username , password = $password")
    }
    regist("zhangsan","111")


    println("----------------------------------------")

    // 参数默认值
       def regist1(username : String, password : String = "222" ) = {
      println(s"username = $username , password = $password")
    }
    regist1("zhangsan","123456")
    regist1("zhangsan")

    println("----------------------------------------")

    //隐式变量
    implicit var defaultPassword : String = "000000"

    //隐式参数  + 柯里化
    def regist2(username : String)(implicit password : String ) = {
      println(s"username = $username , password = $password")
    }

    regist2("lisi")("456789")
    regist2("lisi")

    println("----------------------------------------")

    def regist3(username : String)(implicit password : String = "admin" ) = {
      println(s"username = $username , password = $password")
    }

    regist3("wangwu")("nicai")
    regist3("wangwu")
    regist3("wangwu")()





  }
}
