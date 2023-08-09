package com.atguigu.bigdata.spark.core.test

object TestSort {

    def main(args: Array[String]): Unit = {

        val list = List(
            (20, 1000),
            (30, 1200),
            (20, 2000),
            (30, 1500),
            (20, 1500),
            (30, 1100)
        )

        // 隐式参数没有找到
        // tuple的排序，是先排第一个数据，数据相同，再排第二个，以此类推
        //list.sortBy(t=>t)(Ordering.Tuple2(Ordering.Int, Ordering.Int).reverse).foreach(println)
        list.sortBy(t=>t)(Ordering.Tuple2(Ordering.Int, Ordering.Int.reverse)).foreach(println)

    }
}
