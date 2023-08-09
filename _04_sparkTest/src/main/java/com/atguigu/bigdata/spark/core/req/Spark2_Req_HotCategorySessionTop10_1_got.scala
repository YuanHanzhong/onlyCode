package com.atguigu.bigdata.spark.core.req

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
/*
Top10热门品类中每个品类的Top10活跃Session统计
 */
object Spark2_Req_HotCategorySessionTop10_1_2 {

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

        //过滤数据
        println(objRDD.count())// got  * * *   2021/12/25 21:18  debug的时候, 可以输出下中间结果, 更容易定位问题. 一般不放在()里, 而是放在()外面, 验证()正确执行
        // 180570
        val filterRDD = objRDD.filter(
            obj => {
                if ( obj.click_category_id != -1 ) {
//                    println("\n"+"="*105 + " 2021/12/25 21:19 "+"="*5 + "\n" )
//                    println("*************")

                    top10Id.contains(obj.click_category_id.toString()) // got  * * *   2021/12/25 21:14  contains 要求括号内为同种类型, 这一点很容易出错. 底层用的是 == 判断, == 需要类型一致
                } else {
//                    println("\n"+"="*105 + " 2021/12/25 21:25 "+"="*5 + "\n" + objRDD.count())
                    false // got * * true 和 false一般写在最后, 用来做返回值
                    // got  * * *   2021/12/25 21:23  debug 加标记点的时候, 可以多加几个. scala不擅长跟踪看执行情况, 因为有隐式调用, 难以跟踪. 用中间输出的方式很好
                }

            }
        )

        println(filterRDD.count()) // 看下过滤后的结果
        // 60803

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

    case class UserVisitAction( // got   *  *     2021/12/25 21:02  case class 要写在最外层, 不能写在函数里面
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
