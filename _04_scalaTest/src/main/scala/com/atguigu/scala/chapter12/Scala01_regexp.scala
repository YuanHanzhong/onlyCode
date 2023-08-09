package com.atguigu.scala.chapter12

import scala.util.matching.Regex

/**
  * Scala - 正则表达式
  */
object Scala01_regexp {
  def main(args: Array[String]): Unit = {

    //常用表达式：

    // ^ : 从头匹配
    // $ : 匹配尾
    // . : 一个任意字符
    // * : 0-n
    // + : 1-n
    // ? : 0-1
    //[] : 范围  [a,b,c,d,f]  [a-z A-Z]  [0-9]
    //{} : 次数  {1} {n}  {2,3}
    // | : 表示或者

    // Scala正则语法

    var str : String = "scala is SScalable language , cala"
    //var str : String = "sssssscala"
    val r: Regex = "[s|S]{2}cala".r

    val option: Option[String] = r.findFirstIn(str)
    println(option.getOrElse("匹配不上"))

    println("-------------------------------------")

    val iterator: Regex.MatchIterator = r.findAllIn(str)
    iterator.foreach(println)

    println("-------------------------------------")
    // 需求:匹配手机号
    //

    var phoneNum : String = "18400000000"

    val phoneRex: Regex = "^((13[0-9])|(18[^4]))[0-9]{8}$".r

    val maybeString: Option[String] = phoneRex.findFirstIn(phoneNum)
    println(maybeString.getOrElse("不是手机号"))


  }
}
