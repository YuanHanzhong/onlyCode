package com.atguigu.bigdata.spark.core.req

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark1_Req_HotCategoryTop10_got {

    def main(args: Array[String]): Unit = {

        // TODO 写程序有原则：
        // got  * * *   2021/12/26 10:01   1. 缺什么，补什么
        // got  * * *   2021/12/26 10:01   2. 多什么，删什么. 删指的是filter
        // got   *  *     2021/12/26 10:07  1. filter 2. map 3. reduce

        // TODO 需求一：Top10热门品类
        val conf = new SparkConf().setMaster("local[*]").setAppName("HotCategoryTop10")
        val sc = new SparkContext(conf)

        // TODO 1. 读取文件数据, 是一行一行读取的
        val lineRDD = sc.textFile("data/user_visit_action.txt")

        // TODO 2. 分别统计品类的点击数量，下单数量，支付数量
        // 2.1 统计点击数量
        // (品类，1) => (品类，sum)
        val clickCntDatas = lineRDD.filter(
            line => {
                val datas = line.split("_")
                val categoryId = datas(6)
                categoryId != "-1"
            }
        ).map( // got   *  *     2021/12/26 10:17   map和flatmap不可以互换, 对传入的参数类型有要求
            line => {
                val datas = line.split("_")
                val categoryId = datas(6)
                (categoryId, 1)
            }
        ).reduceByKey(_+_)
        // 2.2 统计下单数量
        // (品类，1) => (品类，sum)
        val orderCntDatas = lineRDD.filter(
            line => {
                val datas = line.split("_")
                val orderIds = datas(8)
                orderIds != "null" // got     *       2021/12/26 10:10  分割出来的是字符串
            }
        ).flatMap( // got     *       2021/12/26 10:13  下单的id可能有多个,
            line => {
                val datas = line.split("_")
                val orderIds = datas(8) // got   *  *     2021/12/26 10:13  命名很有讲究, 使用orderIDs, 代表orderID可能有多个
                val ids = orderIds.split(",")
                ids.map((_, 1))
            }
        ).reduceByKey(_+_)
        // 2.3 统计支付数量
        // (品类，1) => (品类，sum)
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
        println("\n"+"="*105 + " 2021/12/26 10:40 "+"="*5 + "\n" )
        payCntDatas.foreach(println)



        // TODO 3. 按照数量，对结果排序（点击数量 > 下单数量 > 支付数量）
        // TODO 元组的排序：先按照第一个排序，如果相同，按照第二个排序，依此类推
        // (a, 1)
        // (a, 3)
        // (a, (1,3))
        // (品类ID, 点击数量)
        // (品类ID, 下单数量)
        // (品类ID, 支付数量)
        // => join
        // (品类ID, （点击数量，下单数量，支付数量）)
        //val top10: Array[(String, Int)] = clickCntDatas.sortBy(_._2, false).take(10)
        //clickCntDatas.join(orderCntDatas).join(payCntDatas)
        val cogroupRDD: RDD[(String, (Iterable[Int], Iterable[Int], Iterable[Int]))] =
            clickCntDatas.cogroup(orderCntDatas, payCntDatas) // got   *  *     2021/12/26 13:52  cogroup 最多可连全4个

        val mapRDD: RDD[(String, (Int, Int, Int))] = cogroupRDD.mapValues {
            case (iter1, iter2, iter3) => {
                var clickCnt = 0;
                val clickIterator: Iterator[Int] = iter1.iterator
                if (clickIterator.hasNext) {
                    clickCnt = clickIterator.next()
                }

                var orderCnt = 0;
                val orderIterator: Iterator[Int] = iter2.iterator
                if (orderIterator.hasNext) {
                    orderCnt = orderIterator.next()
                }

                var payCnt = 0;
                val payIterator: Iterator[Int] = iter3.iterator
                if (payIterator.hasNext) {
                    payCnt = payIterator.next()
                }

                (clickCnt, orderCnt, payCnt)
            }
        }
        val top10 = mapRDD.sortBy(_._2, false).take(10)

        // TODO 4. 采集数据后，将结果打印在控制台
        top10.foreach(println)

        sc.stop()
    }
}
