package com.atguigu.scala.chapter07

/**
  * Scala - 集合 - 练习2
  */
object Scala14_lianxi2 {
  def main(args: Array[String]): Unit = {
    var datas = List(
      ("zhangsan", "河北", "鞋"),
      ("lisi", "河北", "衣服"),
      ("wangwu", "河北", "鞋"),
      ("zhangsan", "河南", "鞋"),
      ("lisi", "河南", "衣服"),
      ("wangwu", "河南", "鞋"),
      ("zhangsan", "河南", "鞋"),
      ("lisi", "河北", "衣服"),
      ("wangwu", "河北", "鞋"),
      ("zhangsan", "河北", "鞋"),
      ("lisi", "河北", "衣服"),
      ("wangwu", "河北", "帽子"),
      ("zhangsan", "河南", "鞋"),
      ("lisi", "河南", "衣服"),
      ("wangwu", "河南", "帽子"),
      ("zhangsan", "河南", "鞋"),
      ("lisi", "河北", "衣服"),
      ("wangwu", "河北", "帽子"),
      ("lisi", "河北", "衣服"),
      ("wangwu", "河北", "电脑"),
      ("zhangsan", "河南", "鞋"),
      ("lisi", "河南", "衣服"),
      ("wangwu", "河南", "电脑"),
      ("zhangsan", "河南", "电脑"),
      ("lisi", "河北", "衣服"),
      ("wangwu", "河北", "帽子")
    )

    //1. 清洗数据
    val eltDatas: List[(String, String)] = datas.map(t => (t._2,t._3))

    //2. 按照省份 + 商品分组
    val provAndProdGroup: Map[(String, String), List[(String, String)]] = eltDatas.groupBy(t => t )
    println(provAndProdGroup)

    //3.求次数
    val provAndProdCount: Map[(String, String), Int] = provAndProdGroup.mapValues(_.size)
    println(provAndProdCount)

    //4. 按照省份分组
    val provGroup: Map[String, Map[(String, String), Int]] = provAndProdCount.groupBy(_._1._1)
    println(provGroup)

    //5. 处理Value

    val finalResult: Map[String, List[(String, Int)]] = provGroup.mapValues(_.map( t => (t._1._2,t._2)).toList.sortBy(_._2)(Ordering.Int.reverse))
    println(finalResult)
  }
}
