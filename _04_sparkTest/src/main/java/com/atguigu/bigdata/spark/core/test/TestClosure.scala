package com.atguigu.bigdata.spark.core.test

object TestClosure {

    def main(args: Array[String]): Unit = {

        val name = "zhangsan"

        def test(): Unit = {
            println(name)
        }

        test()
        // scala中函数得本质：java中得方法
//        def outer( x : Int ) = {
//            def inner( y : Int ): Unit = {
//                println(x + y)
//            }
//            inner _
//        }
//        outer(10)(20)

    }
}
