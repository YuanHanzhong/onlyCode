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
object Spark001_groupBy_4 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
    val sc = new SparkContext(conf)

    // TODO RDD - 算子 - 转换
    val lineRDD = sc.textFile("data/apache.log")
    val value: RDD[String] = lineRDD.filter(
      line => {
        val datas = line.split(" ")
        val time = datas(3)
        time.startsWith("17/05/2015")
      }
    )
    println("\n"+"="*105 + " 2021/12/23 11:39 "+"="*5 + "\n" )
    value.foreach(println)


      value.map(
      line => {
        val datas = line.split(" ")
        datas(6)
      }
    ).collect().foreach(println)



    sc.stop()


  }

}
