package com.atguigu.scala.chapter04

/**
  * Scala分支控制
  */
object Scala01_if {
  def main(args: Array[String]): Unit = {
    var age : Int  = 25
    //单分支
    if( age >=18 ){
      println("成年")
    }

    println("----------------------")

    //双分支
    if(age >=18){
      println("成年")
    }else{
      println("未成年")
    }

    println("----------------------")

    //多分支

    if(age <= 18 ){
      println("少年")
    }else if ( age <= 30 ){
      println("青年")
    }else if ( age <= 40 ){
      println("壮年")
    }else{
      println("老年")
    }

    println("----------------------")


    // 需求: 根据年龄进行判断，将判断的结果保存到一个变量中.
    // 1.
    var result : String = ""
    if(age <= 18 ){
      result = "少年"
    }else if ( age <= 30 ){
      result = "青年"
    }else if ( age <= 40 ){
      result = "壮年"
    }else{
      result = "中年"
    }

    println("result = " + result )

    println("----------------------")
    //2. 封装方法
    val result1: String = getResult(age)
    println("result1 = " + result1 )

    println("----------------------")
    //3. 表达式的返回值
    //   if表达式的返回值是满足条件的分支中最后一行语句的执行结果

    var result2 = if(age <= 18 ){
      println("*****")
      "少年"
    }else if ( age <= 30 ){
      "青年"
      //123
    }else if ( age <= 40 ){
      "壮年"
    }else{
      "中年"
    }

    println("result2 = " + result2)


    println("----------------------")
    //三元运算符:  boolean表达式 ? 表达式1 : 表达式2
    // if的简写形式

    var result3 = if( age <= 18 ) "少年" else "成年人"

    println("result3 = " + result3)




  }

  def getResult(age : Int) : String = {
    if(age <= 18 ){
      return  "少年"
    }else if ( age <= 30 ){
      return  "青年"
    }else if ( age <= 40 ){
      return "壮年"
    }else{
      return "中年"
    }
  }
}
