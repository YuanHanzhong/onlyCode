package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 至简原则
  */
object Scala04_function {
  def main(args: Array[String]): Unit = {
    //1. 声明完整的函数
    def fun1(): String = {
      return "fun1"
    }

    println(fun1())

    //2. return关键字可以省略.
    def fun2() : String = {
       "fun2"
    }

    println(fun2())

    //3. 如果函数体只有一行语句 , {} 可以省略

    def fun3() : String = "fun3"

    println(fun3())

    //4. 如果能自动推断返回值类型， 返回值类型可以省略

    def fun4()  = "fun4"

    println(fun4())

    //5. 如果函数没有参数，()可以省略
    def fun5  = "fun5"

    println(fun5)

    //5.1 如果函数声明的时候，没有写(),调用的时候不能写()
    //    如果函数声明的时候，有(),但是函数没有参数, ()可以写可以不写.

    def fun55() = "fun55"

    println(fun55())
    println(fun55)


    //6. 返回值类型和return关键字:
    //   如果函数中明确使用了return关键字,返回值类型不能省略
    //   如果函数的返回值类型明确声明为Unit,就算使用return也不管用.

    //   如果函数的返回值类型明确就是Unit,但是又想省略，且还要使用return关键字，
    //   需要将=一并省略，此函数称之为过程函数.

    def fun6 : String  =  return "fun6"

    println(fun6)

    def fun66 : Unit = return "fun66"

    println(fun66)

    def fun666() {
      return "fun666"
    }

    println(fun666)

    //7. 省略def 和 函数名
    //   此函数称之为匿名函数,
    //   匿名函数的语法:  (参数列表) => { 函数体 }
    //   函数类型:  (参数类型) => 返回值类型

    var f = () => "fun7"

    println(f())











  }
}
