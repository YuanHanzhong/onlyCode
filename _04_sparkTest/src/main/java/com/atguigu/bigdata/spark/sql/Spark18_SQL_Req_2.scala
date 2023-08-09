package com.atguigu.bigdata.spark.sql

import org.apache.spark.sql.{Encoder, _}
import org.apache.spark.sql.expressions.Aggregator

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object Spark18_SQL_Req_2 {

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
              |	select
              |	   a.*,
              |	   c.area,
              |	   c.city_name,
              |	   p.product_name
              |	from user_visit_action a
              |	join city_info c on a.city_id = c.city_id
              |	join product_info p on a.click_product_id = p.product_id
              |	where a.click_product_id != -1
              |""".stripMargin).createOrReplaceTempView("t1")

        spark.udf.register("cityRemark", functions.udaf(new CityRemarkUDAF()))

        spark.sql(
            """
              |	select
              |	   area,
              |	   product_name,
              |	   count(*) as clickCnt,
              |    cityRemark(city_name) as cityRemark
              |	from t1 group by area, product_name
              |""".stripMargin).createOrReplaceTempView("t2")

        spark.sql(
            """
              |	select
              |	   *,
              |	   rank() over ( partition by area order by clickCnt desc ) as rank
              |	from t2
              |""".stripMargin).createOrReplaceTempView("t3")

        spark.sql(
            """
              |select
              |   *
              |from t3 where rank <= 3
              |""".stripMargin).show(false)


        spark.stop()

    }
    case class CityRemarkBuffer( var total:Long, var cityMap : mutable.Map[String, Long] )
    // TODO 自定义聚合函数（UDAF）(强类型)
    // 1. 继承类 ：Aggregator
    // 2. 定义泛型
    //    IN : String
    //    BUFF : CityRemarkBuffer
    //    OUT : String
    // 3. 重写方法
    class CityRemarkUDAF extends Aggregator[String, CityRemarkBuffer, String]{
        override def zero: CityRemarkBuffer = {
            CityRemarkBuffer(0L, mutable.Map[String, Long]())
        }

        override def reduce(buffer: CityRemarkBuffer, city: String): CityRemarkBuffer = {
            buffer.total += 1L
            val oldCnt: Long = buffer.cityMap.getOrElse(city, 0L)
            buffer.cityMap.update(city, oldCnt + 1L)
            buffer
        }

        override def merge(b1: CityRemarkBuffer, b2: CityRemarkBuffer): CityRemarkBuffer = {
            b1.total += b2.total
            b2.cityMap.foreach {
                case (city, cnt) => {
                    val oldCnt: Long = b1.cityMap.getOrElse(city, 0L)
                    b1.cityMap.update(city, oldCnt + cnt)
                }
            }
            b1
        }

        override def finish(buffer: CityRemarkBuffer): String = {
            val list = ListBuffer[String]()
            val total: Long = buffer.total // 总共的点击数量
            val sortCitys: List[(String, Long)] = buffer.cityMap.toList.sortBy(_._2)(Ordering.Long.reverse)

            var rest = 100L

            val top2: List[(String, Long)] = sortCitys.take(2)
            top2.foreach {
                case (city, cnt) => {
                    val r = cnt * 100L/total
                    list.append(s"${city} ${r}%")
                    rest -= r
                }
            }
            if ( sortCitys.length > 2 ) {
                list.append(s"其他 ${rest}%")
            }

            list.mkString(", ")
        }

        override def bufferEncoder: Encoder[CityRemarkBuffer] = Encoders.product
        override def outputEncoder: Encoder[String] = Encoders.STRING
    }

}
