package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.util.UUID
import scala.collection.mutable

object SparkStreaming05_Source_DIY {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(3))

        // TODO 自定义数据采集功能
        val ds: ReceiverInputDStream[String] = ssc.receiverStream(new MyReceiver())
        ds.print()

        ssc.start()
        ssc.awaitTermination()

    }
    // TODO 自定义数据采集器 
    //   1. 继承Receiver
    //   2. 定义泛型:采集数据的类型
    //   3. 给父类传递参数 : 存储级别
    //   4. 重写方法（2）
    class MyReceiver extends Receiver[String](StorageLevel.MEMORY_ONLY){
        private var runflg = true
        // TODO 采集器启动后执行逻辑
        override def onStart(): Unit = {

            // TODO 采集数据
            while ( runflg ) {
                val uuid = UUID.randomUUID().toString
                store(uuid)
                Thread.sleep(1000)
            }
        }

        // TODO 采集器停止时执行逻辑
        override def onStop(): Unit = {
            runflg = false
        }
    }
}
