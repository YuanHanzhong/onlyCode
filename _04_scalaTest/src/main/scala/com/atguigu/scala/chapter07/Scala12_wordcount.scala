package com.atguigu.scala.chapter07

import scala.io.Source

/**
  * Scala - 集合 - wordcount
  */
object Scala12_wordcount {
  def main(args: Array[String]): Unit = {
    //需求: 从文件中读取单词数据，实现wordcount

    //1. 读取文件数据，将单词数据维护到集合中
    val datas: List[String] = Source.fromFile("input/word.txt").getLines().toList
    println(datas)

    //2. 将每行数据切分成一个一个的单词
    val words: List[String] = datas.flatMap(_.split(" "))
    println(words)

    //3. 按照单词进行分组
    val wordGroup: Map[String, List[String]] = words.groupBy(word => word )
    println(wordGroup)

    //4. 计算每个单词出现的次数
    //val wordcount: Map[String, Int] = wordGroup.map( kv => (kv._1, kv._2.size) )
    //mapValues: 如果只对map集合中的value进行处理,可以使用mapValues
    val wordcount: Map[String, Int] = wordGroup.mapValues(_.size)

    println(wordcount)

    //5. 按照单词的次数进行排序
    val wordCountSort: List[(String, Int)] = wordcount.toList.sortBy(_._2)(Ordering.Int.reverse)

    println(wordCountSort)

    //6. topn

    val finalResult: List[(String, Int)] = wordCountSort.take(3)

    println(finalResult)

    println("-------------------------------------------------------")

    println(datas.flatMap(_.split(" ")).groupBy(word => word).map(kv => (kv._1, kv._2.size)).toList.sortBy(_._2)(Ordering.Int.reverse).take(3))


  }
}
