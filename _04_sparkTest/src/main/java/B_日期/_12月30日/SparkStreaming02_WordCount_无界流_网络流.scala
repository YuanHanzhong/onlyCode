package B_日期._12月30日

// got

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming02_WordCount_无界流_网络流 {

    def main(args: Array[String]): Unit = {

        // got  *   *        采集器固定步骤

        //1. 配置, 和以前的配置一样
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        //2. 设置上下文, ** StreamingContext **
        val ssc = new StreamingContext(conf, Seconds(2))

        // got  *            socket数据流是一行一行读取的
        val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop102", 8889)

        // 下面逻辑没有变化
        val wordDS = socketDS.flatMap(_.split(" "))
        val wordToOneDS = wordDS.map((_, 1))
        val wordCountDS = wordToOneDS.reduceByKey(_+_)


        wordCountDS.print()
        // got  *            由源码, SprakStreaming 的context.print()打印时间戳

        // 3. ** start() ** 启动采集器
        // 3.1 无界流数据需要专门的数据采集器，所以需要启动采集器
        ssc.start()

        // 4. ** awaitTermination **
        // 4.1 根据输入,
        // 4.2 如果没有await, 会直接执行完, JVM会回收main里所有对象
        ssc.awaitTermination()
    }
}
