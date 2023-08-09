package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming07_Transform_Output {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(3))

        // 从网络流中获取数据（Socket）
        // socket数据流是一行一行读取的
        // 无界流的数据处理，不能关闭环境的
        // 无界流数据需要专门的数据采集器，所以需要启动采集器
        val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop102", 8888)

        val wordDS: DStream[String] = socketDS.flatMap(_.split(" "))
        val wordToOneDS: DStream[(String, Int)] = wordDS.map((_, 1))
        val wordCountDS: DStream[(String, Int)] = wordToOneDS.reduceByKey(_ + _)

        // TODO 默认情况下，一个采集周期内的数据统计结果不会保存到下一个采集周期
        //      计算完毕后，就丢弃不再使用，所以称之为无状态
        wordCountDS.print()

        // 启动采集器, 不能退出driver程序
        ssc.start()
        // Driver程序等待采集器的停止
        ssc.awaitTermination()
        //ssc.stop()

    }
}
