package com.atguigu.scala.chapter02

/**
  * Scala字符串
  */
object Scala04_String {
  def main(args: Array[String]): Unit = {
    //Scala中直接使用Java中的String类.
    //type String        = java.lang.String  (起别名)


    var username : String = "zhangsan"

    var age : Int = 30

    // 字符串的拼接

    // 1. +

    println( "username = " + username + "  , age = " + age )


    // 2. 传值字符串

    printf("username = %s , age = %s " , username ,age )
    println()

    //3. 插值字符串

    println(s"username = $username ,age = $age")

    //4. 多行字符串
    //var sql : String = "select \n\r \tid , count(*) cnt \n\r from user where id > ? group by id having cnt > ? order by cnt desc  "
    //println(sql)

    var sql : String =
      s"""
        |select
        |   id , count(*) cnt
        |from
        |   user
        |where
        |   id > ? and age > $age
        |group by
        |   id
        |having
        |   cnt > ?
        |order by
        |   cnt
        |desc
      """.stripMargin

    println(sql)


  }
}
