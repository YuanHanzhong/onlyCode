package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql._


object Spark17_SQL_Mock {

    def main(args: Array[String]): Unit = {
        System.setProperty("HADOOP_USER_NAME", "root")
        // TODO SparkSQL 环境

        val spark = SparkSession.builder
             .enableHiveSupport() // 启用Hive的支持
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        spark.sql("use atguigu210906")

        spark.sql(
            """
              |CREATE TABLE `user_visit_action`(
              |  `date` string,
              |  `user_id` bigint,
              |  `session_id` string,
              |  `page_id` bigint,
              |  `action_time` string,
              |  `search_keyword` string,
              |  `click_category_id` bigint,
              |  `click_product_id` bigint,
              |  `order_category_ids` string,
              |  `order_product_ids` string,
              |  `pay_category_ids` string,
              |  `pay_product_ids` string,
              |  `city_id` bigint)
              |row format delimited fields terminated by '\t'
              |""".stripMargin)

        spark.sql(
            """
              |load data local inpath 'data/user_visit_action.txt' into table user_visit_action
              |""".stripMargin)

        spark.sql(
            """
              |CREATE TABLE `product_info`(
              |  `product_id` bigint,
              |  `product_name` string,
              |  `extend_info` string)
              |row format delimited fields terminated by '\t'
              |""".stripMargin)

        spark.sql(
            """
              |load data local inpath 'data/product_info.txt' into table product_info
              |""".stripMargin)

        spark.sql(
            """
              |CREATE TABLE `city_info`(
              |  `city_id` bigint,
              |  `city_name` string,
              |  `area` string)
              |row format delimited fields terminated by '\t'
              |""".stripMargin)

        spark.sql(
            """
              |load data local inpath 'data/city_info.txt' into table city_info
              |""".stripMargin)

        spark.sql("select * from city_info").show


        spark.stop()

    }

}
