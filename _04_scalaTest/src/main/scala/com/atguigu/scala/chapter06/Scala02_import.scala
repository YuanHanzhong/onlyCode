package com.atguigu.scala.chapter06



//import java.util.ArrayList
/**
  * Scala - 面向对象编程 - import
  */
object Scala02_import {
  def main(args: Array[String]): Unit = {
    /*
      Java的import:
        1.普通导包(本质上是导入类)
          import java.util.ArrayList ;
          import java.util.* ;

        2.静态导入
           import static

      Scala的import:
        1. 导入包下所有的类
           import  java.util._

        2. import可以在任意位置使用.

        3. import可以实现导包 、 导类 、 导对象.

        4. import可以在同一行中导入多个类

        5. import可以在导包的时候屏蔽包中的某个类

        6. import可以给包中的类取别名

        7. import默认使用相对路径进行导包

        8. scala中默认导入如下内容:
           PreDef._
           java.lang._
           scala._


     */
    //import java.util.ArrayList
    //val arrayList = new ArrayList[String]()

    //导包
    //import java.util
    //val arrayList = new util.ArrayList[String ]()

    //导类
    //import java.util.ArrayList
    //val arrayList = new ArrayList[String ]()

    //导对象
    //val user02 = new User02
    //println(user02.username1)
    //user02.testUser02()

    //import user02._

    //println(username1)
    // testUser02()



    //import java.util.ArrayList
    //import java.util.HashMap

    //import java.util.{ArrayList,HashMap}

    //val arrayList = new ArrayList[String]()

    //val hashMap = new HashMap[String,String]()



    //import java.util._
    //import java.sql.{Date=>_ , _}

    //val date = new Date()




    //import java.util.{Date=>UtilDate , _}
    //import java.sql.{Date=>SqlDate,_}

    //val utilDate = new UtilDate()
    //val sqlDate = new SqlDate(System.currentTimeMillis())


    //import java.util.ArrayList
    //val arrayList = new ArrayList()
    //println(arrayList.getClass.getName)


    //import _root_.java.util.ArrayList
    //val arrayList = new ArrayList[String]()

    //println(arrayList.getClass.getName)









  }

  class User02{

    var username1 : String = "user02"

    def testUser02():Unit = {
      println("testUser02....")
    }

  }
}

package java.util{
  class ArrayList{

  }
}
