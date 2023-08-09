package com.atguigu.bigdata.spark.streaming

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.util.UUID

object SparkStreaming06_Source_Kafka {

  def main(args: Array[String]): Unit = {

    // TODO SparkStreaming 环境
    val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
    val ssc = new StreamingContext(conf, Seconds(1))

    // TODO 从kafka中获取数据
    //   kafka中数据的格式为：K-V
    val kafkaPara: Map[String, Object] = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop102:9092",
      ConsumerConfig.GROUP_ID_CONFIG -> "atguigu",
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

  // TODO 自定义数据采集器
  //   1. 继承Receiver
  //   2. 定义泛型:采集数据的类型
  //   3. 给父类传递参数 : 存储级别
  //   4. 重写方法（2）
  class MyReceiver extends Receiver[String](StorageLevel.MEMORY_ONLY) {
    private var runflg = true

    // TODO 采集器启动后执行逻辑
    override def onStart(): Unit = {

      // TODO 采集数据
      while (runflg) {
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
