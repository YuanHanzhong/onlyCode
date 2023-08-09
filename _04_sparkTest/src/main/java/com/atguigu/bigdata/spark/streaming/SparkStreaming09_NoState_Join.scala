package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming09_NoState_Join {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(5))

        val socketDS1: ReceiverInputDStream[String] = ssc.socketTextStream("localhost", 8888)
        val socketDS2: ReceiverInputDStream[String] = ssc.socketTextStream("localhost", 7777)

        val ds1 = socketDS1.map(
            s => {
                (s, 8888)
            }
        )
        val ds2 = socketDS2.map(
            s => {
                (s, 7777)
            }
        )

        // 双流Join
        //
        ds1.join(ds2).print()


        // 启动采集器, 不能退出driver程序
        ssc.start()
        // Driver程序等待采集器的停止
        ssc.awaitTermination()
        //ssc.stop()

    }
}
