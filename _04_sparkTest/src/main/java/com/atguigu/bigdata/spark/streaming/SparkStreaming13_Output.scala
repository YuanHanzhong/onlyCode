package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming13_Output {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境

        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(3))
        ssc.checkpoint("cp")

        val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop102", 8888)
        socketDS.transform( // 转换
            rdd => {
                rdd
            }
        )
        socketDS.foreachRDD( // 输出
            rdd => {

            }
        )

        ssc.start()
        ssc.awaitTermination()

    }
}
