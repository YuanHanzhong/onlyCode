package com.atguigu.scala.chapter11

/**
  * Scala - 泛型
  */
object Scala01_generic {
  def main(args: Array[String]): Unit = {
    // 0. Java的泛型： <>
    //    Scala的泛型: []

    // 1. 泛型不可变.
    val parentTest: Test[Parent] = new Test[Parent]
    val userTest: Test[User] = new Test[User]

    // 2. 泛型协变[+T]
    //    支持将子类型当成父类型来使用.
    val parentTest1: Test1[Parent] = new Test1[User]
    val userTest1: Test1[User] = new Test1[SubUser]
    val subuserTest1: Test1[SubUser] = new Test1[SubUser]
    val empTest1: Test1[Emp] = new Test1[Emp]


    // 3. 泛型逆变[-T]
    //    支持将父类型当成子类型来使用

    val parentTest2: Test2[Parent] = new Test2[Parent]
    val userTest2: Test2[User] = new Test2[Parent]
    val subuserTest2: Test2[SubUser] = new Test2[User]
    val empTest2: Test2[Emp] = new Test2[Emp]


    //4. 上下限
    val emp : Emp = new Emp()
    val parent : Parent = new Parent()
    val user : User = new User()
    val subuser : SubUser = new SubUser()

    //test[Parent](parent)
    test[User](subuser)
    test[SubUser](subuser)
    //test[Emp](emp)


    test1[Parent](parent)
    test1[User](user)
    //test1[SubUser](subuser)
    //test1[Emp](emp)










  }
  //上限
  def  test[A<:User]( a : A ): Unit = {
    println(a)
  }
  //下限
  def  test1[A>:User]( a : A ): Unit = {
    println(a)
  }



}

class Test2[-T]{

}

class Test1[+T]{

}

class Test[T] {
}

class Parent {
}

class User extends Parent{
}

class SubUser extends User {
}
class Emp{
}


