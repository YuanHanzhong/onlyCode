package com.atguigu.bigdata.spark.core.req

import org.apache.spark.util.AccumulatorV2
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object Spark1_Req_HotCategoryTop10_3 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("HotCategoryTop10")
        val sc = new SparkContext(conf)

        val lineRDD = sc.textFile("data/user_visit_action.txt")

        // TODO 采用累加器的方式实现需求一
        // TODO 1. 创建并注册累加器
        val acc = new HotCategoryAccumulator
        sc.register(acc)

        lineRDD.foreach(
            line => {
                val datas = line.split("_")
                if ( datas(6) != "-1" ) {
                    // 点击行为
                    acc.add((datas(6), "click"))
                } else if ( datas(8) != "null" ) {
                    // 下单行为
                    val ids = datas(8).split(",")
                    ids.foreach(
                        id => {
                            acc.add((id, "order"))
                        }
                    )
                } else if ( datas(10) != "null" ) {
                    // 支付行为
                    val ids = datas(10).split(",")
                    ids.foreach(
                        id => {
                            acc.add((id, "pay"))
                        }
                    )
                }

            }
        )
        // TODO 获取累加器的结果
        val accMap: mutable.Map[String, HotCategoryCnt] = acc.value

        // TODO 将结果进行排序，取前10名
        val hccs: List[HotCategoryCnt] = accMap.toList.map(_._2)
        // sortWith 方法：将你预想的结果返回为true即可
        hccs.sortWith(
            (leftHCC, rightHCC) => {
                if ( leftHCC.clickCnt > rightHCC.clickCnt ) {
                    true
                } else if (leftHCC.clickCnt == rightHCC.clickCnt) {
                    if ( leftHCC.orderCnt > rightHCC.orderCnt ) {
                        true
                    } else if (leftHCC.orderCnt == rightHCC.orderCnt) {
                        leftHCC.payCnt > rightHCC.payCnt
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        ).take(10).foreach(println)


        sc.stop()
    }
    case class HotCategoryCnt( cid:String, var clickCnt : Int, var orderCnt : Int, var payCnt : Int )
    // TODO 热门品类累加器
    //   1.  继承AccumulatorV2
    //   2.  定义泛型
    //       IN : (品类ID, 行为类型)
    //       OUT : Map[ (品类ID, (点击数量，下单数量，支付数量)) ]
    //   3. 重写方法（3 + 3）
    class HotCategoryAccumulator extends AccumulatorV2[ (String, String), mutable.Map[String, HotCategoryCnt] ] {

        private val hotCategoryMap = mutable.Map[String, HotCategoryCnt]()

        override def isZero: Boolean = {
            hotCategoryMap.isEmpty
        }

        override def copy(): AccumulatorV2[(String, String), mutable.Map[String, HotCategoryCnt]] = {
            new HotCategoryAccumulator()
        }

        override def reset(): Unit = {
            hotCategoryMap.clear()
        }

        override def add(v: (String, String)): Unit = {
            val (cid, actionType) = v
            val hcc: HotCategoryCnt = hotCategoryMap.getOrElse(cid, HotCategoryCnt(cid, 0, 0, 0))
            actionType match {
                case "click" => hcc.clickCnt += 1
                case "order" => hcc.orderCnt += 1
                case "pay" =>   hcc.payCnt += 1
            }
            hotCategoryMap.update(cid, hcc)
        }

        override def merge(other: AccumulatorV2[(String, String), mutable.Map[String, HotCategoryCnt]]): Unit = {
            other.value.foreach {
                case (cid, otherHCC) => {
                    val thisHCC: HotCategoryCnt = hotCategoryMap.getOrElse(cid, HotCategoryCnt(cid, 0, 0, 0))
                    thisHCC.clickCnt += otherHCC.clickCnt
                    thisHCC.orderCnt += otherHCC.orderCnt
                    thisHCC.payCnt += otherHCC.payCnt
                    hotCategoryMap.update(cid, thisHCC)
                }
            }
        }

        override def value: mutable.Map[String, HotCategoryCnt] = hotCategoryMap
    }
}
