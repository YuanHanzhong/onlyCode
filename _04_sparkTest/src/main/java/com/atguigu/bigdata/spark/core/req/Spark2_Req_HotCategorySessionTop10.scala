package com.atguigu.bigdata.spark.core.req

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark2_Req_HotCategorySessionTop10 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("HotCategoryTop10")
        val sc = new SparkContext(conf)

        val lineRDD = sc.textFile("data/user_visit_action.txt")
        lineRDD.cache()

        val top10 = lineRDD.flatMap(
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

        // 需求一的ID集合
        val top10Id: Array[String] = top10.map(_._1)

        // TODO 过滤数据
        val filterRDD = lineRDD.filter(
            line => {
                val datas = line.split("_")
                if ( datas(6) != "-1" ) {
                    top10Id.contains(datas(6))
                } else {
                    false
                }
            }
        )

        // TODO 将数据进行格式转换再聚合
        // ((品类，session), sum)
        val reduceRDD = filterRDD.map(
            line => {
                val datas = line.split("_")
                (( datas(6), datas(2) ), 1)
            }
        ).reduceByKey(_+_)

        // TODO 将聚合后的结果进行结构的转换
        // ((品类，session), sum) => (品类, (session, sum))
        val groupRDD: RDD[(String, Iterable[(String, Int)])] = reduceRDD.map {
            case ((cid, session), sum) => {
                (cid, (session, sum))
            }
        }.groupByKey()

        // TODO 将分组后的数据进行组内排序
        val top10Sessions = groupRDD.mapValues(
            iter => {
                iter.toList.sortBy(_._2)(Ordering.Int.reverse).take(10)
            }
        )

        top10Sessions.collect().foreach(println)


        sc.stop()
    }
}
