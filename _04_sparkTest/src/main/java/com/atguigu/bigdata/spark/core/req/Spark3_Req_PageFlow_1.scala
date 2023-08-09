package com.atguigu.bigdata.spark.core.req

import com.atguigu.bigdata.spark.core.req.Spark2_Req_HotCategorySessionTop10_1.UserVisitAction
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark3_Req_PageFlow_1 {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("HotCategoryTop10")
        val sc = new SparkContext(conf)

        val lineRDD = sc.textFile("data/user_visit_action.txt")
        val actionRDD = lineRDD.map(
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
        actionRDD.cache()

        val okIds = List(1L,2L,3L,4L,5L,6L,7L)
        val okZipIds = okIds.zip(okIds.tail)
        // 1-2,2-3,3-4,4-5,5-6,6-7
        // TODO 统计分母
        val pageCntsMap = actionRDD.filter(
            action => {
                okIds.init.contains(action.page_id)
            }
        ).map(
            action => {
                (action.page_id, 1)
            }
        ).reduceByKey(_+_).collect().toMap

        // TODO 将转化结构后的数据按照Session进行分组
        val sessionGroupRDD: RDD[(String, Iterable[UserVisitAction])] =
            actionRDD.groupBy(_.session_id)

        // TODO 将分组后的数据按照访问时间进行排序（升序）
        val mapValueRDD: RDD[(String, List[(Long, Long)])] = sessionGroupRDD.mapValues(
            iter => {
                val timeSortDatas: List[UserVisitAction] = iter.toList.sortBy(_.action_time)
                // 1,2,3,4,5
                // 2,3,4,5
                val pageids: List[Long] = timeSortDatas.map(_.page_id)
                // TODO 过滤分子
                val zipIds = pageids.zip(pageids.tail)
                zipIds.filter(
                    flowid => {
                        okZipIds.contains(flowid)
                    }
                )
            }
        )
        // TODO 计算分子
        val reduceRDD: RDD[((Long, Long), Int)] =
            mapValueRDD
                .map(_._2)
                .flatMap(list => list)
                .map((_, 1))
                .reduceByKey(_ + _)

        // TODO 计算单跳转换率
        reduceRDD.foreach {
            case ((id1, id2), sum) => {
                println(s"页面${id1}跳转到页面${id2}的转换率为：" + (sum.toDouble / pageCntsMap.getOrElse(id1, 0)))
            }
        }
        sc.stop()
    }
}
