package B_日期._12月23日

import org.apache.spark.{SparkConf, SparkContext}

/*
 *需求: 过滤出偶数
 *要点:
 *      1   filter对每一条数据过滤, 只保留为true的
 *      2
 *      3
 */
object Spark002_filter_1 {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setAppName("filter").setMaster("local[*]")
    val sc = new SparkContext(sparkConf)


    sc.makeRDD(List(1,3,5,8,10),2).filter(_ % 2 == 0).foreach(println) // got   *  *     2021/12/23 11:35  过滤出偶数

    sc.stop()

  }
}
