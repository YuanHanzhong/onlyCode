package com
package atguigu
package scala
package chapter06{

  package subchapter06 {

    object Scala01_package_sub{
      def main(args: Array[String]): Unit = {

        Scala01_package.testParentPackage()

        println(username)

        testpackageObject()


      }
    }

  }


  /**
    * Scala - 面向对象 -  包
    */
  object Scala01_package {
    def main(args: Array[String]): Unit = {
      /*
          Java的包:
              1. 管理类
              2. 区分类
                   java.util.Date
                   java.sql.Date
              3. 包权限

          Scala的包:

              1. Scala中包和类的物理路径没有关系。

              2. package可以声明多次

              3. package可以嵌套声明使用 ,子包中的类可以直接访问父包中的内容.

              4. 包可以作为对象使用. 称之为包对象


       */

      println(username)

      testpackageObject()


    }

    def testParentPackage():Unit = {
      println("testParentPackage")
    }
  }


}

