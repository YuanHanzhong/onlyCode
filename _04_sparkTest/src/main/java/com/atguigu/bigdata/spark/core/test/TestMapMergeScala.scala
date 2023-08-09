package com.atguigu.bigdata.spark.core.test

import scala.collection.mutable

object TestMapMergeScala {

    def main(args: Array[String]): Unit = {
        val map1 = mutable.Map(
            ("a", 1), ("b", 2), ("c", 3)
        )
        val map2 = mutable.Map(
            ("a", 4), ("d", 5), ("c", 6)
        )
        var map3 : mutable.Map[String, Int] = null
        map2.foreach {
            case (word, cnt) => {
                val oldCnt = map1.getOrElse(word, 0)
                //map1.update(word, oldCnt + cnt) // 集合不变
                map3 = map1.updated(word, oldCnt + cnt) // 产生新得集合
            }
        }
//        println(map1)
        val mergeMap = map2.foldLeft(map1)(
            (map, kv) => {
                val k = kv._1
                val v = kv._2

                val oldCnt = map.getOrElse(k, 0)
                //map.update(k, oldCnt + v)
                //map
                map.updated(k, oldCnt + v)
            }
        )
        println(map1)
        println(map2)
        //println(mergeMap)
    }
}
