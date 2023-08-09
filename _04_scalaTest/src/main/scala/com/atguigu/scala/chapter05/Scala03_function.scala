package com.atguigu.scala.chapter05

/**
  * Scala - 函数式编程 - 函数参数
  */
object Scala03_function {
  def main(args: Array[String]): Unit = {
    //1. 可变参数
    // 可变参数只能定义到参数列表的最后面.
    // Java  ： String ... names
    // Scala :  names : String*
    def fun1(names  : String*) : Unit = {
      println(s"names = $names")
    }

    fun1()  // List()
    fun1("zhangsan","lisi","wangwu")  //WrappedArray(zhangsan, lisi, wangwu)

    //2.参数默认值

    def regist (username:String, password :String = "000000"):Unit = {
      println(s"username = $username , password = $password")
    }
    regist("admin","123456")
    regist("root")


    //3. 带名参数
    def regist1 (username:String, password :String = "000000" , address : String ):Unit = {
      println(s"username = $username , password = $password , address = $address")
    }

    regist1("admin","123456", "beijing")

    regist1("root",address="shanghai")

    //4. 参数个数

    def fun2(i1:Int, i2:Int,i3:Int,i4:Int,i5:Int,i6:Int,i7:Int,i8:Int,i9:Int,i10:Int,
             i11:Int,i12:Int,i13:Int,i14:Int,i15:Int,i16:Int,i17:Int,i18:Int,i19:Int,
             i20:Int,i21:Int,i22:Int,i23:Int): Unit ={
      println("fun2.....")
    }

    fun2(0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2)

    //当把函数作为值赋值给变量,参数最多只能支持22个.
    //var f  = fun2 _
    //f(0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1)



  }
}
