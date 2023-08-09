package B_日期._12月30日

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}

object SparkStreaming01_通用配置 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")

    // got  *   *        新建StreamingContext 的两种方式
    //1. val ssc = new StreamingContext(conf, Seconds(3)) // 单位为秒, 更常用
    //2. val ssc = new StreamingContext(conf, Duration(3000)) // Duration为样例类, 单位为毫秒

    val ssc = new StreamingContext(conf, Seconds(3))
    // got  *            StreamingContext的第二参数以秒为单位最好, 时间太短浪费, 时间太长则不及时

    // got  *   *        伴生对象
    //    1. 可以直接用, object Seconds{}
    //    2. object顶替了class的位置
    //    3. 在idea中被识别为斜体
    //    4. 图标为黄色的 O

    // got  *   *        想要导入类时, 尽量大写首字母, 容易识别, 容易导入



    // got  *   *        无界流固定写法
    //  使用StreamingContext的最后两个步骤
    //1. ssc.start()
    //2. ssc.awaitTermination

    ssc.start()
    ssc.awaitTermination()
  }
}
