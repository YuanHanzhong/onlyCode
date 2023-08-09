package com.atguigu.bigdata.spark.streaming

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.Date

object SparkStreaming17_Req2_WordCount {
    
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
        
        // TODO 需求二 - 广告点击量实时统计
        // WordCount => State => MySQL
        kafkaValue.map(
            dat => {
                val dats = dat.split(" ")
                val time = dats(0)
                val day = new SimpleDateFormat("yyyy-MM-dd").format(new Date(time.toLong))
                ((day, dats(1), dats(2), dats(4)), 1L)
            }
        ).reduceByKey(_ + _).foreachRDD(
            rdd => {
                rdd.foreach {
                    case ((day, area, city, ad), cnt) => {
                        
                        // 操作Mysql
                        Class.forName("com.mysql.jdbc.Driver")
                        val url = "jdbc:mysql://hadoop102:3306/spark-streaming210906"
                        val user = "root"
                        val password = "root"
                        val conn = DriverManager.getConnection(url, user, password)
                        
                        val pstat = conn.prepareStatement(
                            """
                              | insert into area_city_ad_count (
                              |   dt, area, city, adid, count
                              | ) values (
                              |   ?, ?, ?, ?, ?
                              | )
                              | ON DUPLICATE KEY
                              | UPDATE count=count+?
                              |""".stripMargin)
                        pstat.setString(1, day)
                        pstat.setString(2, area)
                        pstat.setString(3, city)
                        pstat.setString(4, ad)
                        pstat.setLong(5, cnt)
                        pstat.setLong(6, cnt)
                        pstat.executeUpdate()
                        
                        pstat.close()
                        conn.close()
                    }
                }
            }
        )
        
        
        ssc.start()
        ssc.awaitTermination()
        
    }
}
