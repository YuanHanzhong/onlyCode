package com.atguigu.scala.chapter11

/**
  * Scala - 泛型 - 上下文限定
  */
object Scala02_generic {

  def main(args: Array[String]): Unit = {
    // [A] [+A] [-A]  [A >:User] [A<:User]

    def f[A : Test](a: A) = println(a)

    implicit val test : Test[User] = new Test[User]

    f( new User() )

    //f( new SubUser)
  }

}

/*
class Test[T] {
}
class Parent {
}
class User extends Parent{
}
class SubUser extends User {
}
 */
