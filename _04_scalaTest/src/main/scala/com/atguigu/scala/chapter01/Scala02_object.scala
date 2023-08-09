package com.atguigu.scala.chapter01

/**
  * 1.object关键字修饰的,编译后存在两个字节码文件，分别是:
  *    类名.class
  *
  *       反编译成java代码，就是一个普通的类
  *       public final class com.atguigu.scala.chapter01.Scala02_object {
  *       }
  *
  *
  *    类名$.class
  *
  *       反编译成java代码， 就是一个普通的类
  *       public final class com.atguigu.scala.chapter01.Scala02_object$ {
  *       }
  *
  *       在类中包含如下内容:
  *          1) 当前类型的静态变量MODULE$
  *              public static com.atguigu.scala.chapter01.Scala02_object$ MODULE$;
  *
  *          2) 静态代码块
  *             static{
  *                 new ();   //调用构造器创建当前类型的对象。
  *             }
  *
  *          3) 私有构造器
  *              private Scala02_object$(){
  *                 MODULE$ = this;  // 将当前对象赋值在当前类型的静态变量MODULE$
  *             }
  * 2. 在类中添加main方法
  *      类名.class中发现静态的main方法
  *          public static void main(String[] paramArrayOfString){
  *               Scala02_object$.MODULE$.main(paramArrayOfString);  // 调用类名$.class中的普通main方法
  *          }
  *
  *      类名$.class中发现普通的main方法
  *         public void main(String[] args){
  *         }
  *
  * 3. 在main方法中添加代码:
  *      类名.class中没有任何变化
  *           public static void main(String[] paramArrayOfString){
  *                Scala02_object$.MODULE$.main(paramArrayOfString);  // 调用类名$.class中的普通main方法
  *           }
  *      类名$.class中发现添加的代码出现到普通的main方法中
  *         public void main(String[] args){
  *           Predef$.MODULE$.println("Hello Scala object");
  *         }
  *
  * 4. object关键字修饰的执行顺序:
  *    在类名.class中的static main => 类名$.class中的普通main
  *
  *
  * 5. 验证:
  *    在类中添加一个别的方法:
  *       def test(): Unit = {
  *         println("Hello test")
  *       }
  *
  *    底层编译后的效果:
  *       类名.class中:
  *           public static void test(){
  *             Scala02_object$.MODULE$.test();
  *           }
  *
  *       类名$.class中:
  *           public void test(){
  *              Predef$.MODULE$.println("Hello test")
  *           }
  *
  */


object Scala02_object {

  def main(args: Array[String]): Unit = {
    println("Hello Scala object")

    test()
  }

  def test(): Unit = {
    println("Hello test")
  }
}
