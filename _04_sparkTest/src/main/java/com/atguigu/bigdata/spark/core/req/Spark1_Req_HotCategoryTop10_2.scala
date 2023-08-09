package com.atguigu.bigdata.spark.core.req

import org.apache.spark.{SparkConf, SparkContext}

object Spark1_Req_HotCategoryTop10_2 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("HotCategoryTop10")
        val sc = new SparkContext(conf)

        val lineRDD = sc.textFile("data/user_visit_action.txt")
        // (品类ID, 点击数量) => (品类ID, (1, 0, 0))
        // (品类ID, 点击数量) => (品类ID, (1, 0, 0))
        // (品类ID, 点击数量) => (品类ID, (1, 0, 0))
        // (品类ID, 点击数量) => (品类ID, (1, 0, 0))
        // (品类ID, 点击数量) => (品类ID, (1, 0, 0))
        // (品类ID, 下单数量) => (品类ID, (0, 1, 0))
        // (品类ID, 下单数量) => (品类ID, (0, 1, 0))
        // (品类ID, 下单数量) => (品类ID, (0, 1, 0))
        // (品类ID, 下单数量) => (品类ID, (0, 1, 0))
        // (品类ID, 支付数量) => (品类ID, (0, 0, 1))
        // (品类ID, 支付数量) => (品类ID, (0, 0, 1))
        // (品类ID, 支付数量) => (品类ID, (0, 0, 1))
        // (品类ID, 支付数量) => (品类ID, (0, 0, 1))
        // (品类ID, 支付数量) => (品类ID, (0, 0, 1))
        // (品类ID, (点击数量, 下单数量, 支付数量))  (reduceByKey)
        lineRDD.flatMap(
            line => {
                val datas = line.split("_")
                if ( datas(6) != "-1" ) {
                    // 点击的场合
                    List((datas(6), (1, 0, 0)))
                } else if (datas(8) != "null") {
                    // 下单的场合
                    val ids = datas(8).split(",")
                    ids.map(
                        id => {
                            (id, (0, 1, 0))
                        }
                    )
                } else if (datas(10) != "null") {
                    // 支付的场合
                    val ids = datas(10).split(",")
                    ids.map(
                        id => {
                            (id, (0, 0, 1))
                        }
                    )
                } else {
                    Nil
                }
            }
        ).reduceByKey(
            (t1, t2) => {
                (t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3)
            }
        ).sortBy(_._2, false).take(10)
        .foreach(println)


        sc.stop()
    }
}
