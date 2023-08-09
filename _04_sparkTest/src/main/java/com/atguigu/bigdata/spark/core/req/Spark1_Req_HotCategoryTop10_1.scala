package com.atguigu.bigdata.spark.core.req

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark1_Req_HotCategoryTop10_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("HotCategoryTop10")
        val sc = new SparkContext(conf)

        val lineRDD = sc.textFile("data/user_visit_action.txt")
        // TODO 同一个RDD，如果重复使用，那么会多次执行，效率较低，所以增加持久化（缓存）
        lineRDD.cache()

        val clickCntDatas = lineRDD.filter(
            line => {
                val datas = line.split("_")
                val categoryId = datas(6)
                categoryId != "-1"
            }
        ).map(
            line => {
                val datas = line.split("_")
                val categoryId = datas(6)
                (categoryId, 1)
            }
        ).reduceByKey(_+_)

        val orderCntDatas = lineRDD.filter(
            line => {
                val datas = line.split("_")
                val orderIds = datas(8)
                orderIds != "null"
            }
        ).flatMap(
            line => {
                val datas = line.split("_")
                val orderIds = datas(8)
                val ids = orderIds.split(",")
                ids.map((_, 1))
            }
        ).reduceByKey(_+_)

        val payCntDatas = lineRDD.filter(
            line => {
                val datas = line.split("_")
                val payIds = datas(10)
                payIds != "null"
            }
        ).flatMap(
            line => {
                val datas = line.split("_")
                val payIds = datas(10)
                val ids = payIds.split(",")
                ids.map((_, 1))
            }
        ).reduceByKey(_+_)

        // TODO cogroup的问题
        // join(cogroup) : 笛卡尔乘积，shuffle
        //clickCntDatas.join(orderCntDatas)
        // (品类ID, 点击数量) => (品类ID, (点击数量, 0, 0))
        // (品类ID, 下单数量) => (品类ID, (0, 下单数量, 0))

        //                  => (品类ID, (点击数量+0, 0+下单数量, 0+0))
        // (品类ID, 支付数量) => (品类ID, (0, 0, 支付数量))
        //                  => (品类ID, (点击数量+0+0, 0+0+下单数量, 0+0+支付数量))
        // (品类ID, (点击数量, 下单数量, 支付数量))
        val totalCntDatas = clickCntDatas.map {
            case (cid, clickCnt) => {
                ( cid, (clickCnt, 0, 0) )
            }
        }.union(
            orderCntDatas.map {
                case (cid, orderCnt) => {
                    ( cid, (0, orderCnt, 0) )
                }
            }
        ).union(
            payCntDatas.map {
                case (cid, payCnt) => {
                    ( cid, (0, 0, payCnt) )
                }
            }
        )
        val top10 = totalCntDatas.reduceByKey(
            (t1, t2) => {
                (t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3)
            }
        ).sortBy(_._2, false).take(10)

        top10.foreach(println)

        sc.stop()
    }
}
