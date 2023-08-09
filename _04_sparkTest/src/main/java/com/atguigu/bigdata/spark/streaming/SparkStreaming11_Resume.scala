package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.ReceiverInputDStream

object SparkStreaming11_Resume {

  def main(args: Array[String]): Unit = {

    // TODO SparkStreaming 环境
    // 如果想要从检查点恢复数据，那么创建环境的方式就需要发生变化
    val outSSC = StreamingContext.getOrCreate("cp", () => {
      val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
      val ssc = new StreamingContext(conf, Seconds(3))
      ssc.checkpoint("cp")

      val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop102", 8888)

      val wordDS = socketDS.flatMap(_.split(" "))
      val wordToOneDS = wordDS.map((_, 1))
      wordToOneDS.updateStateByKey(
        (seq: Seq[Int], buffer: Option[Int]) => {
          Option(seq.sum + buffer.getOrElse(0))
        }
      ).print()
      ssc
    })

    outSSC.start()
    outSSC.awaitTermination()

  }
}
