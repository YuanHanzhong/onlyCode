package B_日期._12月30日

// got

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreaming02_WordCount_my {

    def main(args: Array[String]): Unit = {

        // got 采集器固定步骤

        //1. 配置, 和以前的配置一样
        val conf = new SparkConf().setMaster("local[*]").setAppName("Spark Streaming")
        //2. 设置上下文, ** StreamingContext **
        val ssc = new StreamingContext(conf, Seconds(2))

        // 从网络流中获取数据（Socket）
        // socket数据流是一行一行读取的
        // 无界流数据需要专门的数据采集器，所以需要启动采集器
        val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop102", 8889)

        val wordDS = socketDS.flatMap(_.split(" "))
        val wordToOneDS = wordDS.map((_, 1))
        val wordCountDS = wordToOneDS.reduceByKey(_+_)


        wordCountDS.print()

//        sss.stop()        // 无界流的数据处理，不能关闭环境的 got

        //3. ** start() ** 
        ssc.start()

        //4. ** awaitTermination **
        ssc.awaitTermination()

    }
}
