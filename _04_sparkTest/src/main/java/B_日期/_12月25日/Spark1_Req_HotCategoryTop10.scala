package B_日期._12月25日

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/*
 *需求: 
 *要点: 
 *      1   
 *      2   
 *      3   
 */
object Spark1_Req_HotCategoryTop10 {
  def main(args: Array[String]): Unit = {

    // TODO 写程序有原则：
    // 1. 缺什么，补什么
    // 2. 多什么，删什么

    // TODO 需求一：Top10热门品类
    val conf = new SparkConf().setMaster("local[*]").setAppName("HotCategoryTop10")
    val sc = new SparkContext(conf)

    // TODO 1. 读取文件数据
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
    ).map(
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
    clickCntDatas.cogroup(orderCntDatas, payCntDatas)

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
