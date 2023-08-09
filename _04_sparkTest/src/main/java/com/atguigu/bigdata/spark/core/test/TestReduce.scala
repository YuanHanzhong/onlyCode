package com.atguigu.bigdata.spark.core.test

object TestReduce {

    def main(args: Array[String]): Unit = {
        val list : List[Int] = List(1,2,3,4) // "1234"

        // (A1, A1) => A1
        // 1. A1泛型和Int应该有关系
        // 2. 2个输入值和返回值类型应该统一
        //list.reduce(_+_)

        // (B, Int) => B
        // 1. B和Int可能没有关系
        // 2. 两个参数之间可以不相同，返回值必须和第一个参数类型相同
        //list.reduceLeft(_+_)

        //list.fold("")(_+_)
        //println(list.foldLeft("")(_ + _))
        // "" + 1 => "1" + 2 => "12" + 3 => "123" + 4 => "1234"

        // 5的阶乘：一个大于1的数据的阶乘等于这个数乘以这个数减一的阶乘
        println(test(5))
    }
    def test( num : Int ):Int = {
        if ( num <= 1 ) {
            1
        } else {
            num * test(num-1)
        }
    }
}
