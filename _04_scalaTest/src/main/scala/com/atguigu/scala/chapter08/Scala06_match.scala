package com.atguigu.scala.chapter08

/**
  * Scala - 模式匹配 - 案例
  */
object Scala06_match {
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
    val etlDatas: List[(String, String)] = datas.map {
      case (name, prov, prod) => (prov, prod)
    }

    //2. 按照省份+商品分组
    val provAndProdGroup: Map[(String, String), List[(String, String)]] = etlDatas.groupBy( word => word )

//    etlDatas.groupBy{
//      case word => word
//    }

    //3.求点击次数
    val prodCount: Map[(String, String), Int] = provAndProdGroup.mapValues(_.size)

//    provAndProdGroup.mapValues{
//      case value => value.size
//    }

    //4. 按照省份分组

    val provGroup: Map[String, Map[(String, String), Int]] = prodCount.groupBy {
      case ((prov, prod), count) => prov
    }

    //5.转换数据结构
    val finalResult: Map[String, List[(String, Int)]] = provGroup.mapValues(
      _.map {
        case ((prov, prod), count) => (prod, count)
      }
        .toList
        .sortBy {
          case (prod, count) => count
        }(Ordering.Int.reverse)
    )
    println(finalResult)

  }
}
