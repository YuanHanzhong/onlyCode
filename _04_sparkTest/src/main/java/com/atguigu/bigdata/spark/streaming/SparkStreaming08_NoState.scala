package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming08_NoState {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(3))

        val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop102", 8888)

//        val wordDS: DStream[String] = socketDS.flatMap(_.split(" "))
//        val wordToOneDS: DStream[(String, Int)] = wordDS.map((_, 1))
//        val wordCountDS: DStream[(String, Int)] = wordToOneDS.reduceByKey(_ + _)

        // TODO 想要完成的功能，DStream做不到，怎么办？
        //      如果RDD能做到，那么可以将DStream转换成RDD来完成
//        wordCountDS.transform(
//            rdd => {
//                rdd.map((_, 1))
//            }
//        ).print()

        // Code Driver
        socketDS.map(
            str => {
                (str, 1) // Code Executor
            }
        )

        // TODO 在Driver端周期性执行的操作也可以采用transform
        // Code Driver (1)
        socketDS.transform(
            rdd => {
                // Code Driver(M)
                rdd.map(
                    str => {
                        (str, 1) // Code Executor(N)
                    }
                )
            }
        )


        // 启动采集器, 不能退出driver程序
        ssc.start()
        // Driver程序等待采集器的停止
        ssc.awaitTermination()
        //ssc.stop()

    }
}
