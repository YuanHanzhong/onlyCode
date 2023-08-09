package com.atguigu.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable

object SparkStreaming04_Source_Queue {

    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(3))

        val rddQueue = new mutable.Queue[RDD[String]]()
        val inputStream = ssc.queueStream(rddQueue,oneAtATime = false)

        val wordDS = inputStream.flatMap(_.split(" "))
        val wordToOneDS = wordDS.map((_, 1))
        val wordCountDS = wordToOneDS.reduceByKey(_+_)

        wordCountDS.print()

        ssc.start()

        for (i <- 1 to 5) {
            rddQueue += ssc.sparkContext.makeRDD(List("Hello Spark"), 10)
            Thread.sleep(3000)
        }

        ssc.awaitTermination()

    }
}
