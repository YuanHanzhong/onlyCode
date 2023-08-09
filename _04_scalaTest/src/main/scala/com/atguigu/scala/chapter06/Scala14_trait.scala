package com.atguigu.scala.chapter06

/**
  * Scala - 面向对象编程 -  特质
  */
object Scala14_trait {
  def main(args: Array[String]): Unit = {

    // 初始顺序: 按照混入的顺序从左到右
    //val mysql14 = new MySQL14


    //功能顺序: 按照混入的顺序从右往左
    // super : 表示上一层特质
    // super[特质]
    val mysql15 = new MySQL15
    mysql15.operData()

  }
}


trait Operator14 {
  println("operator...")
}

trait DB14 {
  println("db...")
}

class MySQL14 extends DB14 with Operator14{
  println("mysql...")
}



trait Operate15{
  def operData():Unit={
    println("操作数据。。")
  }
}

trait DB15 extends Operate15{
  override def operData(): Unit = {
    print("向数据库中。。")
    super.operData()
  }
}

trait Log15 extends Operate15{

  override def operData(): Unit = {
    print("向日志中。。")

    super[Operate15].operData()
  }
}

class MySQL15 extends DB15 with Log15 {

}

