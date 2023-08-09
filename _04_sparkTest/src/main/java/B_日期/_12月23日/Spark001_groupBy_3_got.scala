package B_日期._12月23日

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/*
 *需求: 从服务器日志数据apache.log中获取每个时间段访问量
 *要点:
 *      1
 *      2
 *      3
 */
object Spark001_groupBy_3 {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setAppName("testGroupBy").setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // got     *       2021/12/23 11:17  从文件读取
    val lines: RDD[String] = sparkContext.textFile("data/apache.log")

    // got   *  *     2021/12/23 11:19  对行处理
    lines.map(
      line => {
        val words: Array[String] = line.split(":")
        (words(1), 1)
      }
    ).groupBy(_._1) // got     *       2021/12/23 12:39  当为元组的时候就这样写
      .mapValues(_.size)// got     *       2021/12/23 12:40  元组的时候这样计算值
      .foreach(println)
    sparkContext
      .stop()


  }

}
