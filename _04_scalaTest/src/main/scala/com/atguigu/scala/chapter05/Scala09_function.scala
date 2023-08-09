package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 匿名函数
  */
object Scala09_function {
  def main(args: Array[String]): Unit = {

    def fun( f: String => String ) : Unit = {
      println(f("zhangsan"))
    }

    def fparam(name:String) : String = {
      name.toUpperCase
    }

    fun(fparam)

    println("-----------------------------")
    // 匿名语法 : ( 参数列表 ) => { 函数体 }
    fun( (name:String) => { name.toUpperCase }  )
    // 函数体中只有一行语句,可以省略{}
    fun( (name:String) =>  name.toUpperCase  )
    // 函数的参数类型可以省略
    fun( (name) =>  name.toUpperCase  )
    // 只有一个参数，()可以省略
    fun( name =>  name.toUpperCase  )
    // 函数的参数在函数体中按照顺序只使用一次, 参数名可以省略，通过_来表示参数.
    fun(  _.toUpperCase  )


  }
}
