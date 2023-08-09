package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming12_Window {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境

        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(3))

        val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop102", 8888)

        val wordDS = socketDS.flatMap(_.split(" "))
        val wordToOneDS = wordDS.map((_, 1))
        // TODO 将三个采集周期当成一个整体（窗口）来计算
        // The slide duration of windowed DStream (2000 ms) must be a multiple of the slide duration of parent DStream (3000 ms)
        // 窗口在计算时，窗口的范围以及窗口的滑动幅度（步长）必须为采集周期的整数倍
        val windowDS: DStream[(String, Int)] = wordToOneDS.window(Seconds(9), Seconds(3))
        val wordToCountDS: DStream[(String, Int)] = windowDS.reduceByKey(_ + _)
        wordToCountDS.print()

        ssc.start()
        ssc.awaitTermination()

    }
}
