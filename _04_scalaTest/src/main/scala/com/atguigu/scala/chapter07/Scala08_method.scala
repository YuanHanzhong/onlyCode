package com.atguigu.scala.chapter07

/**
  * Scala - 集合 - 常用方法
  */
object Scala08_method {
  def main(args: Array[String]): Unit = {
    val list = List(1,2,3,4,5)

    //1. 集合长度
    println(list.length)
    println(list.size)

    //2. 判断集合是否为空
    println(list.isEmpty)

    //3. 集合迭代器
    val iterator: Iterator[Int] = list.iterator
    while(iterator.hasNext){
      println(iterator.next())
    }

    println("-------------------------")
    //4. 循环遍历集合
    // 挨个遍历集合中的元素，将每个元素传入到我们指定的函数中进行处理
    //list.foreach(myPrint)
    //list.foreach( (ele:Int) => { println(ele) } )
    //list.foreach( println(_)  )
    //list.foreach( println )

    list.foreach( num => println(num + 1) )

    def myPrint(ele : Int) : Unit = {
      println(ele)
    }

    println("-------------------------")
    //5. 集合头
    println(list.head)

    //6. 集合尾
    println(list.tail)

    //7. 集合最后一个元素
    println(list.last)

    //8. 集合除了最后一个元素
    println(list.init)

    //9 . 集合尾迭代
    val tails: Iterator[List[Int]] = list.tails
    while(tails.hasNext){
      println(tails.next())
    }

    //10 . 集合init迭代
    val inits: Iterator[List[Int]] = list.inits
    //while(inits.hasNext){
      //println(inits.next())
    //}
    inits.foreach(println)

    //11. 将集合转换为字符串
    println(list.mkString(" , "))

    //12. 判断集合是否包含某个元素
    println(list.contains(2))

    //13. 取集合的前几个元素
    println(list.take(3))

    //14. 取集合的后几个元素
    println(list.takeRight(3))

    //15. 查找元素
    //  挨个遍历集合中的元素，传入到我们指定的函数中进行查找,当满足条件后，直接返回查找的结果.
    // 查找集合中满足条件的第一个元素.
    //println(list.find(findMethod))
    //println(list.find( (num:Int) => { num % 2 == 0 }))
    println( list.find(_ % 2 == 0) )

    def findMethod( num : Int) : Boolean = {
      num % 2 == 0
    }

    //16. 丢弃集合中的前几个元素
    println(list.drop(2))

    //17. 丢弃集合中的后几个元素
    println(list.dropRight(2))

    //18. 反转集合
    println(list.reverse)

    //19. 集合去重

    val list1 = List(1,1,2,2,3,3,4,5,6,7)

    println(list1.distinct)


    val list2 = List(1,2,3,4,5)
    val list3 = List(3,4,5,6,7)

    //20. 集合交集
    println(list2.intersect(list3))

    //21. 集合并集
    println(list2.union(list3))

    //22. 集合差集
    println(list2.diff(list3))
    println(list3.diff(list2))

    //23. 切分集合
    println(list.splitAt(3))

    //24. 集合滑动 / 滚动

    list.sliding(3).foreach(println)

    list.sliding(2,2).foreach(println)

    //25. 集合拉链

    val list4 = List(1,2,3,4,5,6)
    val list5 = List("a","b","c","d","e")
    println(list4.zip(list5))

    //26. 集合索引拉链
    println(list5.zipWithIndex)




  }
}
