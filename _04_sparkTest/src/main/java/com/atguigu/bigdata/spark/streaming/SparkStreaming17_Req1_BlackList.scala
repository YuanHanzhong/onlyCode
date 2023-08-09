package com.atguigu.bigdata.spark.streaming

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.sql.{Connection, ResultSet}
import java.text.SimpleDateFormat
import scala.collection.mutable.ListBuffer

object  SparkStreaming17_Req1_BlackList {

    def main(args: Array[String]): Unit = {
        // TODO SparkStreaming
        val conf = new SparkConf().setAppName("Spark Streaming").setMaster("local[*]")
        val ssc = new StreamingContext(conf, Seconds(5))

        // TODO 从Kafka中获取数据
        // 工作中，一般都是从Kafka中获取数据，所以会有专门的工具类完成Kafka的操作
        val kafkaPara: Map[String, Object] = Map[String, Object](
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "linux1:9092,linux2:9092,linux3:9092",
            ConsumerConfig.GROUP_ID_CONFIG -> "atguigu",
            "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
            "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
        )

        // Kafka中数据传输以K-V类型的方式传输的
        val kafkaDS = KafkaUtils.createDirectStream[String, String](
            ssc,
            LocationStrategies.PreferConsistent,
            ConsumerStrategies.Subscribe[String, String](
                Set("bigdata210906"), kafkaPara))

        val kafakValueDS = kafkaDS.map(_.value())

        // TODO 从kafka中将数据采集过来后进行转换
        val adClickDatas = kafakValueDS.map(
            str => {
                val datas = str.split(" ")
                AdClickCnt( datas(0), datas(1), datas(2), datas(3), datas(4) )
            }
        )
        // ********************************************************

        // Code : (读取黑名单的代码不能写在此处，因为当前位置的代码只会执行一次，更新后的黑名单数据无法获取)

        // 1. 【周期性】读取黑名单数据
        // 2. 判断当前数据的用户是否在黑名单中
        //    2.1 如果在黑名单中，数据无效，无需进入后续操作
        //    2.2 如果不在黑名单中，数据有效，执行后续操作
        val reduceData = adClickDatas.transform(
            rdd => {
                val blackList = ListBuffer[String]()
                // TODO 读取黑名单数据
                val conn: Connection = JdbcUtil.getConnection
                val pstat = conn.prepareStatement(
                    """
                      | select userid from black_list
                      |""".stripMargin)
                val rs: ResultSet = pstat.executeQuery()
                while ( rs.next() ) {
                    blackList.append(rs.getString(1))
                }
                rs.close()
                pstat.close()
                conn.close()

                // TODO 判断数据用户是否在黑名单中
                rdd.filter(
                    data => {
                        !blackList.contains(data.user)
                    }
                ).map(
                    data => {
                        val sdf = new SimpleDateFormat("yyyy-MM-dd")
                        val day = sdf.format(new java.util.Date(data.dt.toLong))
                        ((day, data.user, data.ad), 1)
                    }
                ).reduceByKey(_+_)
            }
        )
        // 3. 数据有效时，对数据进行汇总统计（第一次汇总）
        //    3.1 如果汇总的结果超过阈值（100），将用户拉入黑名单，数据无效
        //    3.2 如果汇总的结果没有超过阈值（100），数据有效，执行后续操作
        reduceData.foreachRDD(
            rdd => {
                println("有效数据的处理：")
                rdd.foreachPartition(
                    iter => {
                        val conn = JdbcUtil.getConnection

                        iter.foreach {
                            case ( (day, user, ad), cnt ) => {
                                if ( cnt >= 100 ) {
                                    // TODO 将用户拉入黑名单
                                    val blackListPstat = conn.prepareStatement(
                                        """
                                          | insert into black_list(userid) values (?)
                                          | ON DUPLICATE KEY
                                          | UPDATE userid = ?
                                          |""".stripMargin)
                                    blackListPstat.setString(1, user)
                                    blackListPstat.setString(2, user)
                                    blackListPstat.executeUpdate()
                                    blackListPstat.close()
                                } else {
                                    // TODO 将数据汇总到MySQL的统计表中
                                    val mergePstat = conn.prepareStatement(
                                        """
                                          | insert into user_ad_count (
                                          |     dt, userid, adid, count
                                          | ) values (
                                          |     ?, ?, ?, ?
                                          | )
                                          | ON DUPLICATE KEY
                                          | UPDATE count = count + ?
                                          |""".stripMargin)
                                    mergePstat.setString(1, day)
                                    mergePstat.setString(2, user)
                                    mergePstat.setString(3, ad)
                                    mergePstat.setInt(4, cnt)
                                    mergePstat.setInt(5, cnt)
                                    mergePstat.executeUpdate()
                                    mergePstat.close()

                                    // TODO 读取数据，判断是否超过阈值
                                    val loadPstat = conn.prepareStatement(
                                        """
                                          | select userid
                                          | from user_ad_count
                                          | where dt = ? and userid = ? and adid = ?
                                          | and count >= 100
                                          |""".stripMargin)
                                    loadPstat.setString(1, day)
                                    loadPstat.setString(2, user)
                                    loadPstat.setString(3, ad)
                                    val rs: ResultSet = loadPstat.executeQuery()
                                    if ( rs.next() ) {
                                        // TODO 数据超过阈值，应该拉入黑名单
                                        val blackListPstat = conn.prepareStatement(
                                            """
                                              | insert into black_list(userid) values (?)
                                              | ON DUPLICATE KEY
                                              | UPDATE userid = ?
                                              |""".stripMargin)
                                        blackListPstat.setString(1, user)
                                        blackListPstat.setString(2, user)
                                        blackListPstat.executeUpdate()
                                        blackListPstat.close()
                                    }

                                    rs.close()
                                    loadPstat.close()
                                }
                            }
                        }

                        conn.close()
                    }
                )
            }
        )

        // 4. 数据有效时，对数据进行全天汇总统计（第二次汇总）

        // 5. 读取汇总数据，判断是否超过阈值（100）
        //    5.1 如果汇总的结果超过阈值（100），将用户拉入黑名单，数据无效
        //    5.2 如果汇总的结果没有超过阈值（100），数据有效，执行后续操作


        ssc.start()
        ssc.awaitTermination()
    }
    case class AdClickCnt( dt : String, area:String, city : String, user:String, ad:String )
}
