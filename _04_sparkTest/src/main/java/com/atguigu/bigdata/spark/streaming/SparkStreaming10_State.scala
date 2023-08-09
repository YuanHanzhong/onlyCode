package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming10_State {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(3))
        ssc.checkpoint("cp")

        val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop102", 8888)

        val wordDS = socketDS.flatMap(_.split(" "))
        val wordToOneDS = wordDS.map((_, 1))
        // TODO 如果想要让数据有状态的保存
        // updateStateByKey的参数类型中的seq表示相同key的value的集合
        // requirement failed: The checkpoint directory has not been set. Please set it by StreamingContext.checkpoint().
        // 这种方式会导致小文件过多，影响文件读取效率，不推荐使用。
        // 一般会采用第三方中间件（redis）完成，联合无状态操作
        wordToOneDS.updateStateByKey(
            (seq : Seq[Int], buffer:Option[Int]) => {
                Option(seq.sum + buffer.getOrElse(0))
            }
        ).print()


        // 启动采集器, 不能退出driver程序
        ssc.start()
        // Driver程序等待采集器的停止
        ssc.awaitTermination()
        //ssc.stop()

    }
}
