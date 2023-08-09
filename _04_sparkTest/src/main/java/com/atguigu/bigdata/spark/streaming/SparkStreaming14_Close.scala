package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming14_Close {

    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(3))
        ssc.socketTextStream("hadoop102", 8888).print()
        //ssc.stop()
        ssc.start()
        val t1 = new Thread(new Runnable {
            override def run(): Unit = {
                // 代码无法判断业务和技术变数
                // 一般会采用中间件记录系统的状态
                // MySQL => table => flg => false
                // Redis => map => flg => false
                // HDFS  => /path
                // ZK  => /node
//                while ( true ) {
//                    // Load JDBC Table
//                    Thread.sleep(10000)
//                    if ( flg == true ) {
//                        ssc.stop(true, true) // 强制关闭
//                    }
//                }

            }
        })
        t1.start()
        //t1.stop()// 真正地停止
        // i++ => 1. 赋值，（stop）2.加1
        // long(64), double(64)
        // 32 + 32,  32 + 32

        ssc.awaitTermination() // 阻塞
        //ssc.stop()

    }
}
