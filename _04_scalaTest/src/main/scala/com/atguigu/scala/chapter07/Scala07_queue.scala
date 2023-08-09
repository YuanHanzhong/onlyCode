package com.atguigu.scala.chapter07

import scala.collection.mutable

/**
  * Scala - 集合 - 队列
  */
object Scala07_queue {
  def main(args: Array[String]): Unit = {
    // 队列 : FIFO
    // Queue
    val queue = new mutable.Queue[String]()

    // 入队
    queue.enqueue("a")
    queue.enqueue("b")
    queue.enqueue("c")
    queue.enqueue("d","e","f")

    println(queue)

    // 出队

    val str: String = queue.dequeue()
    println(str)

    println(queue)
  }
}
