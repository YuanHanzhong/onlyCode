package com.atguigu.bigdata.spark.streaming

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.io.{File, PrintWriter}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer

object SparkStreaming16_Req3_LastHourAnalysis_1 {
    def main(args: Array[String]): Unit = {

        // TODO SparkStreaming 环境
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        val ssc = new StreamingContext(conf, Seconds(5))

        // TODO 从kafka中获取数据
        //   kafka中数据的格式为：K-V
        val kafkaPara: Map[String, Object] = Map[String, Object](
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop102:9092,hadoop103:9092,hadoop104:9092",
            ConsumerConfig.GROUP_ID_CONFIG -> "atguigu",
            "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
            "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
        )

        val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
            KafkaUtils.createDirectStream[String, String](ssc,
                LocationStrategies.PreferConsistent,
                ConsumerStrategies.Subscribe[String, String](Set("bigdata210906"), kafkaPara))

        val kafkaValue: DStream[String] = kafkaDStream.map(_.value())

        // TODO 需求三 - 最近一分钟，每10秒某个广告点击量趋势统计

        // TODO 类似的需求都采用窗口操作 : 窗口范围（一分钟），窗口步长（10秒）

        // word => (word, 1) => (word, cnt)
        // time => timeX => (timeX, 1) => (timeX, cnt)
        kafkaValue.map(
            dat => {
                val dats = dat.split(" ")
                val time = dats(0)
                // 15:06 => 15:00
                // 15:16 => 15:10
                // 15:19 => 15:10
                // 15:59 => 15:50
                (time.toLong / 10000 * 10000, 1)
            }
        )
        .window(Seconds(60), Seconds(10))
        .reduceByKey(_+_)
        .foreachRDD(
            rdd => {
                val timeDatas: Array[(Long, Int)] = rdd.collect()

                val buffer = ListBuffer[String]()
                timeDatas.sortBy(_._1).foreach {
                    case (time, cnt) => {
                        val ms = new SimpleDateFormat("mm:ss").format(new Date(time.toLong))
                        buffer.append(
                            s"""
                              |{ "xtime":"${ms}", "yval":"${cnt}" }
                              |""".stripMargin)
                    }
                }

                val out = new PrintWriter(new File("D:\\onedrive\\01_正吸收\\014_专业\\code\\My_04_Spark\\data\\adclick\\adclick.json"))
                out.println("["+buffer.mkString(",")+"]")
                out.flush()
                out.close()
            }
        )


        ssc.start()
        ssc.awaitTermination()

    }
}
