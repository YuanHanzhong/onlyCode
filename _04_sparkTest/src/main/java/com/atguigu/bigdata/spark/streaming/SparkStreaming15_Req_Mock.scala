package com.atguigu.bigdata.spark.streaming

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.util.{Properties, Random}
import scala.collection.mutable.ListBuffer

object SparkStreaming15_Req_Mock {

    def main(args: Array[String]): Unit = {

        // TODO 向Kafka中写入数据
        val topic = "bigdata210906"
        val prop = new Properties()
        // 添加配置
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop102:9092")
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        val producer = new KafkaProducer[String, String](prop)

        while ( true ) {
            for ( dat <- genDatas() ) {
//                println(dat)
                val record = new ProducerRecord[String, String](topic,dat)
                producer.send(record)
            }
            Thread.sleep(5000)
        }
    }
    private def genDatas(): ListBuffer[String] = {

        val areas = List("华北", "东北", "华南")
        val citys = List("北京", "天津", "上海", "深圳")

        val buffer = ListBuffer[String]()
        for ( i <- 1 to new Random().nextInt(50) ) {

            val ts = System.currentTimeMillis()
            val area = areas(new Random().nextInt(areas.length))
            val city = citys(new Random().nextInt(citys.length))
            val user = new Random().nextInt(6) + 1
            val ad = new Random().nextInt(6) + 1

            buffer.append(s"${ts} ${area} ${city} ${user} ${ad}")
        }
        buffer
    }
}
