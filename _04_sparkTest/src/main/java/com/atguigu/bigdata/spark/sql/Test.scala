package com.atguigu.bigdata.spark.sql

import java.sql.DriverManager

/*
 *需求: 
 *要点: 
 *      1   
 *      2   
 *      3   
 */
object Test {

  def main(args: Array[String]): Unit = {
    val conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "root")
    println(conn)
  }
}
