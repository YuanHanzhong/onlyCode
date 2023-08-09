package com.atguigu.scala.chapter07

/**
  * Scala - 集合 -  常用方法
  */
object Scala10_method {
  def main(args: Array[String]): Unit = {
    //40. map : 按照指定的规则(函数)将集合转换成另一个集合.
    //          map转换前与转换后的集合的元素个数不变
    val list = List(1,2,3,4,5)
    println(list.map(_ + 1))

    val list2 = List("a","b","c","d","e")
    println(list2.map(_.toUpperCase))


    //41. flatten : 扁平化.  将集合中的元素处理成一个一个的个体.

    val list3 = List( List(1,2) , List(3,4,5) , List(7,8))
    println(list3.flatten)

    val list4 =
      List( List( List(1,2) ,List(3,4) ) , List( List(3,4,5) ) , List( List(7,8) , List(10,11)))
    println(list4.flatten.flatten)

    //42. flatMap ： map + flatten

    val list5 = List( List(1,2),List(3,4))

    println(list5.flatten.map(_ * 2))
    // map -> List( List(2,4) , List(6,8 )) -> flatten -> List(2,4,6,8)
    println(list5.flatMap(_.map(_ * 2)))

    val list6 = List("hello scala","hello spark")
    // map -> List( Array(hello ,scala) , Array(hello,spark)  ) -> flatten -> List(hello, scala, hello, spark)
    println(list6.flatMap(_.split(" ")))

    //43. filter : 过滤.  按照指定的规则将集合中的元素进行过滤，满足条件的保留，不满足条件的丢弃.
    //    find :  查找,

    val list7 = List(1,2,3,4,5)

    println(list7.filter(_ % 2 == 0))

    val list8 = List("hello","spark","scala","hadoop")

    println(list8.filter(_.startsWith("h")))

    //44. groupBy: 分组.  按照指定的规则将集合中的元素进行分组.
    //                    将每个组的分组规则计算的结果作为key,每个组的元素所在的集合作为Value.

    val list9 = List(1,2,3,4)

    println(list9.groupBy(_ % 2))
    println(list9.groupBy(_ % 2 == 0))

    val list10 = List("hello","hello","scala","haha")

    println(list10.groupBy(word => word))

    println(list10.groupBy(_.substring(0, 1)))

    //45. sortBy :  排序. 按照指定的规则将集合中的元素进行排序.
    val list11 = List(3,1,4,2)
    println(list11.sortBy(num => num))
    println(list11.sortBy(num => num)(Ordering.Int.reverse))

    val list12 = List( (30,"zhangsan"), (20,"wangwu") ,(20,"lisi") )
    println(list12.sortBy(t => t))

    println(list12.sortBy(t => t)(Ordering.Tuple2(Ordering.Int.reverse, Ordering.String)))


    //46. sortWith : 自定义排序
    println(list12.sortWith(
      (t1, t2) => {
        if (t1._1 != t2._1) {
          t1._1 > t2._1 // 降序
        } else {
          t1._2 < t2._2 // 升序
        }
      }
    ))








  }
}
