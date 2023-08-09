package B_日期._12月23日

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/*
 *需求: 
 *要点: 
 *      1   
 *      2   
 *      3   
 */
object Spark001_groupBy_1 {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setAppName("testGroupBy").setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // got     *       2021/12/23 10:28  有了sc就可以makeRDD了
    val dataRDD: RDD[Int] = sparkContext.makeRDD(List(1,3,5),2)

    // got     *       2021/12/23 10:32  有了data就可以RDD了
    val groupData: RDD[(Int, Iterable[Int])] = dataRDD.groupBy(_%2)

    println("\n"+"="*105 + " 2021/12/23 10:37 "+"="*5 + "\n" +groupData)
    groupData.foreach(println)


  }

}
