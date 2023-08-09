package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql._


object Spark18_SQL_Req {

    def main(args: Array[String]): Unit = {
        System.setProperty("HADOOP_USER_NAME", "root")
        // TODO SparkSQL 环境

        val spark = SparkSession.builder
             .enableHiveSupport() // 启用Hive的支持
            .master("local[*]")
            .appName("SparkSQL")
            .getOrCreate()

        spark.sql("use atguigu210906")

        // TODO 1. 缺什么，补什么
        // user_visit_action ：地区,商品名称，
        // TODO 2. 根据地区，商品对数据分组统计
        //     （(地区，商品)，1）=>（(地区，商品)，sum）
        // TODO 3. 按照地区进行分组，然后对商品点击进行排序（降序），取前三名
        //  （(地区，商品)，sum） => （地区, (商品，sum)）

        spark.sql(
            """
              |select
              |   *
              |from (
              |	select
              |	   *,
              |	   rank() over ( partition by area order by clickCnt desc ) as rank
              |	from (
              |		select
              |		   area,
              |		   product_name,
              |		   count(*) as clickCnt
              |		from (
              |			select
              |			   a.*,
              |			   c.area,
              |			   c.city_name,
              |			   p.product_name
              |			from user_visit_action a
              |			join city_info c on a.city_id = c.city_id
              |			join product_info p on a.click_product_id = p.product_id
              |			where a.click_product_id != -1
              |		) t1 group by area, product_name
              |	) t2
              |) t3 where rank <= 3
              |""".stripMargin).show


        spark.stop()

    }

}
