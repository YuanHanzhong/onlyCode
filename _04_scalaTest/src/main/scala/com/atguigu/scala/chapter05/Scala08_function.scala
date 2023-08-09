package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 匿名函数
  */
object Scala08_function {
  def main(args: Array[String]): Unit = {
    //1. 定义一个函数, 函数的参数类型为: 函数类型
    def fun( f : (Int, Int ) => Int ) : Unit = {
      println(f(10, 20))
    }

    def fparam(num1:Int,num2:Int ) : Int = {
      num1 + num2
    }

    fun(fparam)
    println("---------------------------")
    // 匿名函数:  (参数列表) => {函数体}

    fun(
      (num1:Int,num2:Int ) => {num1 + num2}
    )
    println("---------------------------")
    // 匿名函数 - 至简原则
    // 1. 如果函数体只有一行语句,{}可以省略
    fun( (num1:Int,num2:Int ) => num1 + num2 )

    //2. 函数的参数类型可以省略
    fun( (num1,num2) => num1 + num2 )

    //3. 如果函数的参数只有一个，()可以省略

    //4. 如果函数的参数在函数体中按照参数的顺序只使用一次,
    //   参数名、() 、 =>都可以省略
    //   使用_表示参数

    fun( _ + _ )
  }
}
