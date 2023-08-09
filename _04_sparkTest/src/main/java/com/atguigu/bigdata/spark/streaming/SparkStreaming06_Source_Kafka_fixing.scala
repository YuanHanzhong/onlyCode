package com.atguigu.bigdata.spark.streaming

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.util.UUID

object SparkStreaming06_Source_Kafka_fixing {

  def main(args: Array[String]): Unit = {

    // TODO SparkStreaming 环境
    val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
    val ssc = new StreamingContext(conf, Seconds(3))

    // TODO 从kafka中获取数据
    //   kafka中数据的格式为：K-V
    val kafkaPara: Map[String, Object] = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop102:9092, hadoop103:9092, hadoop104:9092",
      ConsumerConfig.GROUP_ID_CONFIG -> "bigdata210906",  // ask
          // my:  2021/12/30 22:50  
          // 老师: 
          // 下一次: 
      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
    )

    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
      KafkaUtils.createDirectStream[String, String](
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](Set("bigdata210906"), kafkaPara)
      )

    val kafkaValue: DStream[String] = kafkaDStream.map(_.value())

    kafkaValue.print()

    ssc.start()
    ssc.awaitTermination()
  }

}
