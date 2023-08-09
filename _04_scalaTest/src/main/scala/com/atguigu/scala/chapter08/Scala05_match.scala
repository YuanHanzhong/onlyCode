package com.atguigu.scala.chapter08

/**
  * Scala - 模式匹配 - 应用场景
  */
object Scala05_match {
  def main(args: Array[String]): Unit = {
    //1.变量声明
    val tuple: (String, Int, Boolean) = ("zhangsan",30,false)
    println("常规访问: " + tuple._1 + " : " + tuple._2 + " : " + tuple._3 )

    tuple match {
      case (name,age,flag) => println(name + " : " + age +" : " + flag)
      case _ => println("匹配不成功")
    }


    var (name,age,flag) =  ("zhangsan",30,false)
    println(name + " : " + age  + " : " + flag  )

    val array = Array(1,2,3,4,5)

    array match {
      case Array(first,second,thrid,forth,five)  => "ok"
    }

    var Array(first,second,thrid,forth,five) = Array(1,2,3,4,5)

    println("------------------------------------------")

    //2. 循环匹配
    val map = Map("A" -> 1, "B" -> 0, "C" -> 3)

    //需求: 迭代map中的kv
    for (elem <- map) {
      println(elem._1  + " --> " + elem._2)
    }

    for (elem <- map) {
      elem match {
        case (k,v) => println(k + " ==> "+ v )
      }
      //println(elem._1  + " --> " + elem._2)
    }

    for ( (k,v)<- map) {

       println(k + " ==> "+ v )
      //println(elem._1  + " --> " + elem._2)
    }

    println("-----------------------")
    //遍历value=0的 k-v ,如果v不是0,过滤

    for ( (k,0) <- map) {

      println(k + " ==> "+ 0)
      //println(elem._1  + " --> " + elem._2)
    }


    println("------------------------------------------------")

    //3. 函数参数
    // 将如下list中的tuple的第二个元素乘以2
    val list = List(("a",1),("b",2),("c",3))

    //常规写法
    println(list.map(t => (t._1, t._2 * 2)))

    //模式匹配
    println(list.map{
      case (k,v )=> (k,v*2 )
    })


    //获取map中的value的第二个元素乘以2的结果
    val map1 = Map("a"->("aa",1),"b"->("bb",2),"c"->("cc",3))

    //常规写法
    println(map1.map(t => (t._1, (t._2._1, t._2._2 * 2))))

    println(map1.map{
      case (outk,(innerk,innerv)) => (outk,(innerk,innerv * 2 ))
    })

  }
}
