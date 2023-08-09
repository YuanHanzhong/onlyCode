package B_日期._12月23日

import org.apache.spark.{SparkConf, SparkContext}

/*
 *需求: 将List("Hello", "hive", "hbase", "Hadoop")根据单词首写字母进行分组
 *要点:
 *      1
 *      2
 *      3
 */
object Spark001_groupBy_2 {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setAppName("testGroupBy").setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // got     *       2021/12/23 10:42  提示识别不了makeRDD, 是因为返回的类型不对
    val dataRDD = sparkContext.makeRDD(List("Hello", "hive", "hbase", "Hadoop"))

    // got   *  *     2021/12/23 10:47  groupby之后, 要变成集合collect, 集合才能用foreach
    dataRDD.
      groupBy(word => word.head).foreach(println) // got   *  *     2021/12/23 11:00  取第一个字母, 可以直接 .head

    sparkContext.stop()


  }

}
