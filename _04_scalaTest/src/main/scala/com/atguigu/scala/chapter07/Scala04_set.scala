package com.atguigu.scala.chapter07

import scala.collection.mutable

/**
  * Scala - 集合 - Set
  */
object Scala04_set {
  def main(args: Array[String]): Unit = {
    // 不可变 Set
    val set: Set[Int] = Set(1,2,2,3,4,4,5,5,6)
    println(set)

    // 可变 Set

    val mset: mutable.Set[Int] = mutable.Set(5,6,7,7,8,8,9,10,11)
    println(mset)

    mset.add(12)
    println(mset)
    //mset.add(11)
    //println(mset)

    mset.remove(11)
    println(mset)

    mset.remove(20)


    mset.update(5,true)
    println(mset)

    mset.update(5,false)
    println(mset)

    mset.update(20,true)
    println(mset)

    mset.update(30,false)
    println(mset)


    //集合交集
    val set1: Set[Int] = Set(1,2,3,4)
    val set2: Set[Int] = Set(3,4,5,6)

    val set3: Set[Int] = set1 & set2
    println(set3)

    //集合差集
    val set4: Set[Int] = set2 &~ set1
    println(set4)





  }
}
