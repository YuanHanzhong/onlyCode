package com.atguigu.scala.chapter01

/**
  * 1. class关键字修饰的,编译后只有一个字节码文件：
  *      类名.class
  *          通过反编译成java代码发现就是一个普通的类:
  *          public class com.atguigu.scala.chapter01.Scala03_class
  *
  * 2. 在类中添加main方法
  *       反编译后发现就是一个普通的main方法
  *       public void main(String[] args){
  *       }
  *
  * 3. 在main方法中添加代码:
  *       反编译后发现添加的代码出现到普通的main方法中
  *       public void main(String[] args){
  *           Predef$.MODULE$.println("Hello Scala class");
  *       }
  *
  * 4. class关键字修饰的执行顺序：
  *       new Scala03_class().main()
  *
  */
class Scala03_class {
  def main(args: Array[String]): Unit = {
    Predef.println("Hello Scala class")
  }
}
