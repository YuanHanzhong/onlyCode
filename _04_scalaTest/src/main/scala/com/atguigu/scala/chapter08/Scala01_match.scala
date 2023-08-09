package com.atguigu.scala.chapter08

/**
  * Scala - 模式匹配
  */
object Scala01_match {
  def main(args: Array[String]): Unit = {
    /*
      语法: 对象 match {
              case 匹配规则  => 分支体
              case .......
            }

            Scala的模式匹配会从上往下依此进行匹配， 当匹配上某个分支，会执行分支的代码，执行完就直接返回.
            当所有的分支都不满足， 会执行case _分支. 如果没有提供case_分支，会抛出异常.
            case_分支要写到最后面.
     */
    var a: Int = 10
    var b: Int = 20

    var operator: Char = '+'

    var result = operator match {

      case _ => "illegal"
      case '+' => a + b
      case '-' => a - b
      case '*' => a * b
      case '/' => a / b

    }
    println(result)

  }
}
