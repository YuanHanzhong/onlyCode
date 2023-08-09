package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming03_Source_Dir {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(3))

        val fileDS: DStream[String] = ssc.textFileStream("input")

        val wordDS = fileDS.flatMap(_.split(" "))
        val wordToOneDS = wordDS.map((_, 1))
        val wordCountDS = wordToOneDS.reduceByKey(_+_)

        wordCountDS.print()

        ssc.start()
        ssc.awaitTermination()

    }
}
