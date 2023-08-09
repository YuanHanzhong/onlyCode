package com.atguigu.bigdata.spark.core.req

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark2_Req_HotCategorySessionTop10_1 {

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

        // ********************* 需求二 *****************************
        val objRDD = lineRDD.map(
            line => {
                val datas = line.split("_")
                UserVisitAction(
                    datas(0),
                    datas(1).toLong,
                    datas(2),
                    datas(3).toLong,
                    datas(4),
                    datas(5),
                    datas(6).toLong,
                    datas(7).toLong,
                    datas(8),
                    datas(9),
                    datas(10),
                    datas(11),
                    datas(12).toLong
                )
            }
        )

        // TODO 过滤数据

        // Executor计算时，如果使用了Driver端的数据，那么必须将Driver端的数据传递到Executor
        // 这个数据的传输以Task为单位。但是会造成大量的数据冗余
        // 简单的解决方法就是将数据以Executor端传递，那么多个Task共享数据
        // RDD没有这个能力，需要采用新的数据模型 ： 广播变量

        // TODO 封装广播变量
        val bc: Broadcast[Array[String]] = sc.broadcast(top10Id)
        val filterRDD = objRDD.filter(
            obj => {
                if ( obj.click_category_id != -1 ) {
                    // TODO 使用广播变量
                    bc.value.contains(obj.click_category_id.toString)
                } else {
                    false
                }
            }
        )

        // TODO 将数据进行格式转换再聚合
        // ((品类，session), sum)
        val reduceRDD = filterRDD.map(
            obj => {
                (( obj.click_category_id, obj.session_id ), 1)
            }
        ).reduceByKey(_+_)

        // TODO 将聚合后的结果进行结构的转换
        // ((品类，session), sum) => (品类, (session, sum))
        val groupRDD: RDD[(Long, Iterable[(String, Int)])] = reduceRDD.map {
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
    case class UserVisitAction(
          date: String,//用户点击行为的日期
          user_id: Long,//用户的ID
          session_id: String,//Session的ID
          page_id: Long,//某个页面的ID
          action_time: String,//动作的时间点
          search_keyword: String,//用户搜索的关键词
          click_category_id: Long,//某一个商品品类的ID
          click_product_id: Long,//某一个商品的ID
          order_category_ids: String,//一次订单中所有品类的ID集合
          order_product_ids: String,//一次订单中所有商品的ID集合
          pay_category_ids: String,//一次支付中所有品类的ID集合
          pay_product_ids: String,//一次支付中所有商品的ID集合
          city_id: Long//城市 id
      )
}
