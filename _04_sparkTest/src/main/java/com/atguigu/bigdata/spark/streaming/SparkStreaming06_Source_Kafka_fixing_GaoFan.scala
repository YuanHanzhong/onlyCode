package com.atguigu.bigdata.spark.streaming



import java.util.UUID

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming05_Source_Kafka_consumer_gaofan {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("sparkWordCount")
    /* todo
        StreamingContexgt环境对象中第二个参数表示数据采集周期(以秒为单位)
     */
    val ssc = new StreamingContext(conf, Seconds(3))
    /*
        todo
           1.从kafka获取数据
           2.kafka中数据格式为:k-v
     */
    val kafkaPara: Map[String, Object] = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop102:9092,hadoop103:9092,hadoop104:9092",
      ConsumerConfig.GROUP_ID_CONFIG -> "gf",
      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
    )

    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
      KafkaUtils.createDirectStream[String, String](ssc,
        LocationStrategies.PreferConsistent,    // todo 位置策略
        ConsumerStrategies.Subscribe[String, String]  //todo 消费策略 -- 订阅的topic(k,v)
          (Set("bigdata210906"), kafkaPara))  //todo

    /*
        todo
          kafka命令：
            kafka-topics.sh --bootstrap-server hadoop102:9092 --list   ----> 查看kafka中多少个topic
            kafka-topics.sh --bootstrap-server hadoop102:9092 --create topic sparkStreaming --paritition 3 --replication-factor 2  创建topic
            kafka-console-producer.sh --broker-list  hadoop102:9092,hadoop103:9092,hadoop104:9092 --topic test
     */
    val kafkaValue: DStream[String] = kafkaDStream.map(_.value())
    kafkaValue.print()
    ssc.start()
    ssc.awaitTermination()
  }
  /*
      todo 自定义数据采集器
          1.继承Recceiver
          2.定义泛型:采集数据的类型
          3.给父类传递参数: 存储级别
          4.重新方法(2)

     */
  class MyReceiver extends Receiver[String](StorageLevel.MEMORY_ONLY){

    private var runflag=true
    //todo 采集器启动后执行逻辑
    override def onStart(): Unit = {

      //todo 采集数据
      while (runflag){
        val uuid: String = UUID.randomUUID().toString
        //todo 存储uuid
        store(uuid)
        Thread.sleep(1000)
      }
    }
    //todo 采集器停止时执行逻辑
    override def onStop(): Unit = {
      runflag =false
    }
  }
}

