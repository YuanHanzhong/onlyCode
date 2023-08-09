package com.atguigu.scala.chapter07

import scala.collection.mutable

/**
  * Scala - 集合 - map
  */
object Scala05_map {
  def main(args: Array[String]): Unit = {
    // 不可变  Map
    // 键值 :  k -> v
    val map: Map[String, Int] = Map( "a"->1 , "b"->2 , "c"->3, "d"->4 , "e" -> 5  , "e"->55 , "d" ->44 )
    println(map)

    // 可变 Map
    val mmap: mutable.Map[String, Int] = mutable.Map( "a"->1 , "b"->2 , "c"->3, "d"->4 , "e" -> 5  , "e"->55 , "d" ->44 )

    println(mmap)

    mmap.put("f",6)
    println(mmap)

    mmap.remove("e")
    println(mmap)

    mmap.remove("h")
    println(mmap)

    mmap.update("a",11)
    println(mmap)

    mmap.update("h",77)
    println(mmap)

    //通过key获取Value

    val value: Int = mmap.apply("h")   //NoSuchElementException: key not found: hh
    println(value)

    val option: Option[Int] = mmap.get("h")
    if(option.isEmpty){
      println("获取不到value")
    }else{
      val value: Int = option.get
      println("获取到的Value: " + value)
    }

    val v = option.getOrElse( "获取不到...." )
    println(v)
    val v1: Any = mmap.getOrElse("hh" , "NO")
    println(v1)


    // map与其他集合的转换
    // k-> v  == (k,v)  元组
    val array: Array[(String, Int)] = mmap.toArray
    println(array.mkString(" , "))
    val list: List[(String, Int)] = mmap.toList
    println(list)
    val set: Set[(String, Int)] = mmap.toSet
    println(set)


    // map的迭代
    //单独迭代key
    val keys: Iterable[String] = mmap.keys
    keys.iterator

    val iterator: Iterator[String] = mmap.keysIterator

    val set1: collection.Set[String] = mmap.keySet


    //单独迭代value
    val valuesIterator: Iterator[Int] = mmap.valuesIterator

    val values: Iterable[Int] = mmap.values
    values.iterator


    //迭代 kv

    for (elem <- mmap) {}



  }
}
