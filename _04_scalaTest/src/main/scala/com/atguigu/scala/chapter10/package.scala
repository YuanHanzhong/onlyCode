package com.atguigu.scala

package object chapter10 {

  implicit  def transform(num : Double) :Int = {
    println("包对象作用域....")
    num.toInt
  }
}
