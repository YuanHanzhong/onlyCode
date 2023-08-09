package B_日期._12月30日

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.util.UUID

object SparkStreaming06_Source_Kafka_my {

  def main(args: Array[String]): Unit = {

    // TODO SparkStreaming 环境
    val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
    val ssc = new StreamingContext(conf, Seconds(3))

    // TODO 从kafka中获取数据
    //   kafka中数据的格式为：K-V

    val kafkaPara: Map[String, Object] = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop102:9092,hadoop103:9092,hadoop104:9092",
      // got  *            9092为kafka端口号
      ConsumerConfig.GROUP_ID_CONFIG -> "atguigu", // atguigu 为组名,
      // got  *            kafka消费时, 以消费者组为单位. 不同的的消费者组有不同的进度, 所以有偏移量
      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer", // 反序列化
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
    )

    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] =
      KafkaUtils.createDirectStream[String, String]( // 经常通过kafka工具类去链接
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](// 第一个string定义key, 第2个定义v
          Set("spark"), // 后面的topic可以为多个
          kafkaPara)
        // got  *   *        para args 通常表示参数
      )
        // got  *            kafka中数据格式为K-V 键值对, 通过K和分区策略决定放在哪个分区. K只在存入时起作用, 消费时不起作用

    val kafkaValue: DStream[String] = kafkaDStream.map(_.value())

    kafkaValue.print()


    ssc.start()
    ssc.awaitTermination()

  }

/*
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
  }*/

}
