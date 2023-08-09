
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming12_Window_1 {

  def main(args: Array[String]): Unit = {

    // TODO SparkStreaming 环境

    val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
    val ssc = new StreamingContext(conf, Seconds(3))
    ssc.checkpoint("cp")

    val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop102", 8888)

    val wordDS = socketDS.flatMap(_.split(" "))
    val wordToOneDS = wordDS.map((_, 1))
    // The checkpoint directory has not been set. Please set it by StreamingContext.checkpoint().
    wordToOneDS.reduceByKeyAndWindow(
      (x, y) => { // 添加到窗口中的数据计算
        println(x + "+" + y)
        x + y
      },
      (x, y) => { // 移出窗口的数据计算
        println(x + "-" + y)
        x - y
      },
      Seconds(9),
      Seconds(6) // 计算时间
    ).print()

    ssc.start()
    ssc.awaitTermination()

  }
}
