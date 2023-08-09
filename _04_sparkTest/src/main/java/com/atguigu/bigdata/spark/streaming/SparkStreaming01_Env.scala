package com.atguigu.bigdata.spark.streaming
// 已全部吸收
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}

object SparkStreaming01_Env {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境
        // SparkStreaming有自己的环境对象
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        // StreamingContext环境对象中的第二个参数表示数据采集周期（以秒为单位）
        val ssc = new StreamingContext(conf, Seconds(3))
        ssc.stop()

    }
}
