
package B_日期._12月23日
import org.apache.spark.{SparkConf, SparkContext}
object Spark04_RDD_Oper_Transform_Test_flatMap_case {
    /*
    需求: 使用偏函数筛选出List
    要点:
         1. 借助flatmap, 降维
            1.1 List中有个Int, 不能被查, 所以用模式匹配
         2. 降维后, 每个元素, 用模式匹配
    */
    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(conf)

        val rdd = sc.makeRDD(List(List(1,2), 3, List(4,5)))

        rdd.flatMap{
            case lis : List[_] => { // got 01  *  *     2021/12/23 13:56  匹配List, 类型就写 List[_]
                lis
            }
            case other => {
                List(other) // got 02 *  *     2021/12/23 14:26  想要foreach, 需要用List包装下
            }
        }.collect().foreach(println)
        sc.stop()

    }
}
