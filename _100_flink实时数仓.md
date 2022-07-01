# 按逻辑

## 整体架构

![image-20220628115640721](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220628115640721.png)

![image-20220628205824737](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220628205824737.png)

![image-20220628093441370](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220628093441370.png)

### ODS![image-20220627233826369](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627233826369.png)

1.   





### DWD

![image-20220627233924971](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627233924971.png)

![image-20220627233901854](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627233901854.png)

### DWM

![image-20220627233953052](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627233953052.png)

### DWS

![image-20220627234035416](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627234035416.png)

### ADS

![image-20220627234058824](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627234058824.png)

## 日志采集服务

![image-20220627233419126](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627233419126.png)

## 分区, 分组, 分流

![image-20220627233502400](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627233502400.png)

## 业务数据分流

![image-20220627233529037](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627233529037.png)

## 实时热度关键词

![image-20220627233349920](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220627233349920.png)



# 指标以及实现思路

## 流量主题

### 各渠道流量统计





# 遇到的问题和优化

### 配置信息分流 

### 做旁路缓存

1.   没有把缓存放在状态而放在redis, 为了减轻OOM

### 存Hbase而不存Redis



# 边缘知识点

## MySQL

 1.   MySQL主从复制过程 
      1.   ①Master主库将改变记录，写到二进制日志(binary log)中 
      2.   ②Slave从库向mysql master发送dump协议，将master主库的binary log events 拷贝到它的中继日志(relay log) 
      3.   ③Slave从库读取并重做中继日志中的事件，将改变的数据同步到自己的数据库。 
 2.   MySQL的binlog MySQL的二进制日志可以说MySQL最重要的日志了，它记录了所有的DDL和DML(除 了数据查询语句)语句，以事件形式记录，还包含语句所执行的消耗的时间，MySQL 的二进制日志是事务安全型的。 
 3.   二进制有两个最重要的使用场景 
      1.   MySQL Replication在Master端开启binlog，Master把它的二进制日志传递给 slaves来达到master-slave数据一致的目的。 
      2.   数据恢复，通过使用mysqlbinlog工具来使恢复数据。 
 4.   二进制日志包括两类文件 
      1.   ①二进制日志索引文件（文件名后缀为.index）用于记录所有的二进制文件 
      2.   ②二进制日志文件（文件名后缀为.00000*）记录数据库所有的DDL和DML(除了数 据查询语句)语句事件。 
 5.   binlog的分类 
      1.   ①statement：
           1.   语句级，binlog会记录每次一执行写操作的语句。 
           2.   优点：有可能造成数据不一致。
           3.   缺点：有可能造成数据不一致。 比如取系统时间. 
      2.   row：
           1.   行级， binlog会记录每次操作后每行记录的变化。 
           2.   优点：保持数据的绝对一致性，只记录执行后的效果。
           3.   缺点：占空间 
      3.   mixed：
           1.   一定程度上解决了，因为一些情况而造成的statement模式不一致问题。 
           2.   默认还是statement，在某些情况下，会按照 ROW的方式进行处理。
           3.   有些极个别情况 依旧会造成不一致，另外statement和mixed对于需要对binlog的监控的情况都不方便。

## ngix

Nginx1.Nginx负载均衡 

常用的负载均衡策略：轮询、权重、备机… 

2.nginx占用80端口，默认情况下非root用户不允许 

使用1024以下端口 

让当前用户的某个应用也可以使用1024以下的端口： 

sudo setcap cap_net_bind_service=+eip 

/opt/module/nginx/sbin/nginx 

3.静态代理：把所有静态资源的访问改为访问 

nginx，而不是访问tomcat，因为nginx更擅长于静 

态资源的处理，性能更好，效率更高。 

4.Nginx的负载均衡和静态代理结合在一起，可以实 

现动静分离



## Springboot

Springboot 

内嵌Tomcat,不再需要外部的Tomcat 

更方便的和各个第三方工具 

（mysql,redis,elasticsearch,dubbo,kafka等整 

合），而只要维护一个配置文件即可。 

springboot就是把以前需要用户手工配置的部 

分，全部作为默认项。“约定大于配置”。如果 

需要特别配置的时候，去修改 

application.properties（application.yml） 



## maxwell

Maxwell 

1.Maxwell实时读取MySQL二进制日志Binlog，并生成 

JSON 格式的消息，作为生产者发送给 Kafka 

2.Maxwell工作原理 

把自己伪装成slave，假装从master复制数据 

3.Maxwell想做监控分析，选择row格式比较合适 

canal 

1.canal工作原理 

把自己伪装成Slave，假装从Master复制数据 

2.Cannel想做监控分析，选择row格式比较合适 

3.canal使用场景 

①更新缓存 

②抓取业务数据新增变化表，用于制作拉链表。 

③抓取业务表的新增变化数据，用于制作实时统计 

![image-20220629100130982](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220629100130982.png)

## logger

logger.sh 

1.在LoggerController中添加方法，将日志落盘并发送到 

Kafka主题中 @RestController = 

@Controller+@ResponseBody @RestController //表示 

返回普通对象而不是页面 

2.在Resources中添加logback.xml配置文件 

①appender：追加器，描述如何写入到文件中（写在哪， 

格式，文件的切分） 

. 

②ConsoleAppender--追加到控制台RollingFileAppender- 

-滚动追加到文件 

③logger：控制器，描述如何选择追加器 

④日志级别：

trace、debug、info、warn、error、fatal 

3.日志采集模块-打包集群部署，并用Nginx进行反向代理 

logger.sh：将采集日志服务（nginx和采集日志数据的jar启 

动服务）放到脚本中

## FlinkCDC

FlinkCDC 

1.核心思想: 

监测并捕获数据库的变动（包括数据或数据表的插入、更新以及删除等），将这些变 

更按发生的顺序完整记录下来，写入到消息中间件中以供其他服务进行订阅及消费。 

2.CDC的种类 

![image-20220628230001064](C:/Users/Jack/AppData/Roaming/Typora/typora-user-images/image-20220628230001064.png)

FlinkCDC中使用DataStream，不是使用FlinkSQL 

DataStream优点在于可以多库多表监控，缺点：写反序列化，

.tostring无法使用 

FlinkSQL可以直接转为JavaBean，缺点：只能单表 

FlinkCDC、Maxwell、Canal的区别 

共同点：支持断点续传 

不同点：FlinkCDC封装的数据格式为自定义，能够根据库批量初始化，可以自定义 

Maxwell封装的数据格式为单条数据JSON，只能够单表初始化，不可以自定义 

Canal封装的数据个格式为单条SQL JSON，如果一条sql影响多条数据，数据封装 

在JSON数组，不能进行初始化，可以自定义，但只能是服务端和客户端 

1.维度数据为什么保存在HBase而不是Kafka 

Kafka默认保存七天，而维度数据要永久保留 

2.为什么从phoenix而不从MySQL中获取 

为了减小数据库的压力，存在MySQL中，维度数据关联的较多，实时不断有数据插入， 

查询请求压力会很大。使用binlog导出 

3.为什么使用HBase而不使用Redis 

redis隔段时间要落盘，而用户维度数据量很大。hbase可以根据rowkey查出数据明细。 

但hbase查询效率低，所以用redis做旁路缓存，加异步IO 

## 





5.   1.   

# 备忘

### todo

-   [ ] 组件的版本以及日期
-   [ ] 数据量
-   [ ] 

##  Flink版本

1.   FlinkCDC 从2.11开始出来
2.   Flink1.13 是2020年发布, 我们使用是在





# 按日期

## day01 2022年6月28日

1.   实时类别
     1.   普通实时
          1.   不分层, 直接计算
     2.   实时数仓
          1.   分层, 复用
2.   掌握程度
     1.   能说清, 画出图来
3.   经过Kafka
     1.   消峰
     2.   解耦(更灵活)
          1.   实时离线都可以从Kafka取
4.   离线架构再次用flume
     1.   `零点漂移`, 使用拦截器做处理
          1.   零点漂移就是Flume sink 到HDFS上的时候, 不指定时间戳, 时间就是写入的时间, 就写到了到了第二天
     2.   减Flume压力
     3.   隔离开生产方和消费方, 方便其他应用使用
5.   实时架构
6.   数仓建模意义
     1.   方便存取, 分析
7.   数仓建模方法
     1.   2类
          1.   ER
               1.   注重实体, 关系
               2.   规则
                    1.   范式(2,3 都为减少冗余. 能1个推出来的, 绝不要两个)
                         1.   第一范式: 单元格不可切分
                         2.   第二范式: 不存在部分依赖(联合主键的一部分可以推出来的, 要自立)
                         3.   第三范式: 不存在传递依赖(凡事能自己推导出来的, 都要自立)
          2.   维度模型
               1.   事实
                    1.   业务过程, 
                         1.   过程不可拆分的行为
               2.   维度
                    1.   发生的环境
               3.   事实表
                    1.   核心
                    2.   有维度外键
                    3.   有度量
                    4.   类别
                         1.   事务型事实表
                              1.   原子操作
                              2.   最细粒度
                              3.   设计过程
                                   1.   选择业务过程
                                   2.   声明粒度, 每行代表什么, 尽量细, 越细越灵活
                                   3.   确定维度, 业务的环境, 尽量丰富
                                   4.   `确认事实`
                              4.   `不足`
                                   1.   库存, 余额
                                        1.   我只要结果, 但每次都要从头到尾算
                                   2.   多个表(事物)关联, 下单到支付的平均时间
               4.   周期型快照事实表
                    1.   解决存量的, 我直接存结果
               5.   累积型快照事实表
                    1.   解决多表关联, 我直接把关联结果存起来
                         1.   表中多个日期, 每个日期对应一个业务过程
          3.   维度表
               1.   设计步骤	
               2.   维度模型
                    1.   雪花模型, 要很多join
                    2.   星型模型, 就1级
                    3.   `星座模型`
               3.   实时
                    1.   `不再维度退化`, 
8.   数仓设计
     1.   `项目是亮点, 说的非常清楚`
          1.   分层
          2.   流程
               1.   ![image-20220628105232390](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220628105232390.png)

1.   数据调研
     1.   非常清楚业务过程
     2.   需求
2.   明确数据域
     1.   相当于加了书架
     2.   就是分类, 找的时候好找
3.   构建业务总线矩阵, 依据它做DIM和DWD
     1.   ![image-20220628112420002](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220628112420002.png)
4.   明确统计指标, 做DWS
     1.   确认指标, 标准化, 统一口径, 避免歧义, 避免重复
     2.   类别
          1.   原子指标, 不可拆分, 
               1.   某一业务过程
               2.   度量值
               3.   聚合逻辑
          2.   派生指标
               1.   ![image-20220628113425197](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220628113425197.png)
          3.   衍生指标
               1.   多个派生指标计算
          4.   作用
               1.   轻度聚合, 减少日后计算和存储压力
               2.   抽象出公共的, 减少重复编码和计算

14点00分

1.   `maxwell和datax区别`, 

2.   不再`维度退化`

     1.   历史数据
     2.   本身多个流有时差
     3.   全都存到HBase, 用到join

3.   pom依赖

     1.   最短层
     2.   从上往下

4.   kafka分区, 改为4和flink并行度一样, 为了严格有序, 从而去重. 

     1.   kafka分区数不能比flink并行度多, 会导致无序
     2.   kafka分区数不能比flink少, 会有水位线问题

     # 

6.   `GRANT ALL  ON maxwell.* TO 'maxwell'@'%' IDENTIFIED BY 'root';`

7.   

## day02 2022年6月29日

#### dim 选型

1.   mysql, redis, clickhouse, Hbase

     1.   mysql查询效率较低
     2.   主要做关联, kv比较合适
          1.   kv性能更好
          2.   redis, 对内存压力大
          3.   Hbase, 性能好, 可以处理大数据
               1.   根据key查询
               2.   有缓存

#### hbase

#### 怎么存维度表

	1. 如何识别哪些是维度表
    	1. 规范表名, 正则匹配
        	1. 表名需要改业务
        	2. 有的表有双重属性
	2. 用表做映射
    	1. 灵活
	3. 那么表放在哪里
    	1. redis
        	1. 有变化,  不能监控到
    	2. mysql
        	1. 可以利用CDC(maxwell), 监控binlog, 一有变化就同步数据
    	3. 每次启动都把信息读入, 不用每次都查, 维表个数变化时, 有问题
        	1. 放open, 每5秒查一次, 有些浪费, 因为维表不会经常变化
        	2. 



![image-20220629094519466](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220629094519466.png)



#### 没有用flinkCDC 全部替换maxwell 10点22分

1.   稳定, flink1.1
     1.   低版本, 同步时锁
     2.   名字在变, MySQL
     3.   兼容性问题, 比如要删除security
2.   影响架构, 和log处理架构就不同了
3.   失去kafka的优势
4.   maxwell可以处理某张表同步历史数据, 而flinkCDC只能全部

#### 配置表的作用

1.   过滤
2.   存储表的配置信息, 方便自动建表
     1.   要存储的信息
          1.   ![image-20220629103449352](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220629103449352.png)
3.   

#### 为什么配置流用广播

1.   因为业务流并行度为4, 每个并行都要

#### 两个流如何工作

`双流join`

1.   ![image-20220629105110114](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220629105110114.png)



`检查点对齐`

​	exaxtly once, 

​	barry

`状态后端`



#### `kafka精准一次`

手动提交offset, 默认的是先提交, 后处理. 改成先处理, 后提交. 

flink的API默认精准一次










###      09_上午内容回顾.mp4
###      10_日志数据分流代码思路.mp4
###      11_基本环境准备以及检查点设置.mp4
###      12_从Kafka中读取数据以及封装kafka工具类中的获取Consumer对象的方法.mp4
###      13_对流中数据进行类型转换以及测试.mp4
###      14_新老访客标记修复思路以及分组实现.mp4
###      15_状态的声明以及初始化.mp4
###      16_新老访客标记修复代码实现.mp4
###      17_日志数据分流.mp4
### 
## day03 2022年7月1日

`状态`



先把框架搭出来, 再磨细节, 做优化

#### 拼接建表sql

#### 执行sql

使用JDBC

![image-20220701110629969](https://pictures-for-typora.oss-cn-beijing.aliyuncs.com/typora_imageimage-20220701110629969.png)





放到open, 只创建1次

优化: 使用连接池









###      00_内容回顾.mp4
###      01_将不同流数据写到kafka主题.mp4
###      02_动态分流思路1.mp4
###      03_动态分流思路2.mp4
###      04_FlinkCDC介绍.mp4
###      05_上午内容回顾.mp4
###      06_CDC介绍以及配置库准备.mp4
###      07_FlinkCDC读取MySQL数据_API方式.mp4
###      08_检查点记录FinkCDC读取数据的偏移量.mp4
###      09_FlinkCDC读取MySQL数据_SQL方式.mp4
###      10_自定义反序列化器.mp4
###      11_业务数据分流代码思路.mp4
###      12.mp4
### 
## day04
###      00_内容回顾.mp4
###      01_barrier对齐理解.mp4
###      02_配置表以及对应的实体类创建.mp4
###      03_使用FlinkCDC读取配置信息.mp4
###      04_抽取TableProcessFunction类完成动态分流.mp4
###      05_处理广播流数据.mp4
###      06_通过Debug熟悉广播流处理流程.mp4
###      07_处理主流数据.mp4
###      08_动态分流基本思路总结.mp4
###      09_上午内容回顾.mp4
###      10_提前创建维度表思路分析.mp4
###      11_拼接建表语句.mp4
###      12_执行建表语句.mp4
###      13_建表常见问题说明.mp4
###      14_字段过滤.mp4
### 
## day05
###      00_内容回顾.mp4
###      01_维度侧输出流数据写入思路分析.mp4
###      02_拼接upsert语句.mp4
###      03_使用JDBC执行upsert语句.mp4
###      04_执行测试以及常见问题解决.mp4
###      05_重写获取FlinkKafkaProducer方法.mp4
###      06_重写获取支持精准一次的FlinkKafkaProducer的方法.mp4
###      07_业务数据整体分流测试.mp4
###      08_业务数据动态分流思路总结.mp4
###      09_DWM业务介绍.mp4
###      10_UV独立访客思路分析.mp4
###      11_从kafka中读取数据.mp4
###      12_状态失效时间设置.mp4
###      13_独立访客过滤实现.mp4
###      14_独立访客整体测试以及流程总结.mp4
### 
## day06
###      00_内容回顾.mp4
###      01_跳出行为分析.mp4
###      02_用户跳出实现思路分析.mp4
###      03_从kafka中读取数据.mp4
###      04_Watermark指定以及事件时间字段提取.mp4
###      05_pattern定义思路分析.mp4
###      06_pattern的定义以及应用到流上.mp4
###      07_从流中提取数据方式介绍.mp4
###      08_从流中提取超时数据.mp4
###      09_用户跳出明细测试.mp4
###      10_订单宽表实现思路分析.mp4
###      11_订单宽表代码实现思路分析.mp4
###      12_从kafka中读取订单和订单明细主题数据.mp4
###      13_定义订单和明细实体类.mp4
###      14_对流中的数据类型进行转换.mp4
###      15_Watermark的指定以事件时间字段提取.mp4
###      16_intervalJoin介绍.mp4
###      17_双流join的实现.mp4
###      18_订单和明细关联测试.mp4
###      19_intervalJoin底层实现分析.mp4
### 
## day07
###      00_内容回顾.mp4
###      01_封装PhoenixUtil工具类以及实现思路.mp4
###      02_连接对象的创建.mp4
###      03_对结果集的处理1.mp4
###      04_对结果集的处理2.mp4
###      05_查询维度的工具类的封装.mp4
###      06_维度查询工具类整体实现.mp4
###      07_旁路缓存介绍.mp4
###      08_redis启动注意事项.mp4
###      09_封装操作Redis的工具类.mp4
###      10_上午内容回顾.mp4
###      11_旁路缓存实现思路分析.mp4
###      12_旁路缓存代码实现.mp4
###      13_维度数据修改删除缓存.mp4
###      14_基本维度关联实现.mp4
###      15_异步IO分析.mp4
###      16_发送异步请求思路.mp4
###      17_双重校验锁解决单例设计模式懒汉式线程安全问题.mp4
###      18_总结.mp4
###      Redis面试题-先更新数据库还是先更新缓存.docx
###      
### 
## day08
###      00_内容回顾.mp4
###      01_维度存储数据库选择疑问解答.mp4
###      02_抽取发送异步请求的类DimAsyncFunction.mp4
###      03_模板方法设计进行异步维度关联思路.mp4
###      04_用户维度关联实现.mp4
###      05_和用户维度关联测试.mp4
###      06_和地区维度关联.mp4
###      07_和商品维度关联.mp4
###      08_和其他维度进行关联以及写到kafka主题.mp4
###      09_订单宽表实现总结.mp4
###      10_模板方法设计模式应用场景.mp4
###      11_从kafka中读取数据.mp4
###      12_从Kafka中读取数据测试.mp4
###      13_对流中的数据类型进行转换.mp4
###      14_指定Watermark以及封装日期工具类.mp4
###      15_支付和订单宽表双流join以及整体测试.mp4
### 
## day09
###      00_内容回顾.mp4
###      01_DWS层说明.mp4
###      02_访客主题统计思路分析.mp4
###      03_从kafka主题中读取数据.mp4
###      04_将pageLog流数据类型进行转换.mp4
###      05_合并各个流的数据并测试.mp4
###      06_按照维度进行分组.mp4
###      07_开窗.mp4
###      08_聚合操作.mp4
###      09_聚合操作测试.mp4
###      10_ClickHouse介绍.mp4
###      11_上午内容回顾.mp4
###      12_安装准备工作.mp4
###      13_单机安装.mp4
###      14_整数数据类型.mp4
###      15_其它数据类型.mp4
###      16_partitionby.mp4
###      17_primaryKey和orderBy.mp4
###      18_二级索引以及TTL(了解).mp4
###      19_ReplacingMergeTree.mp4
###      20_SummingMergeTree.mp4
### 
## day10
###      00_内容回顾.mp4
###      01_SQL操作.mp4
###      02_副本写入流程.mp4
###      03_副本zk服务器的配置.mp4
###      04_副本测试.mp4
###      05_分片集群读写流程.mp4
###      06_分片集群配置.mp4
###      07_分片集群测试.mp4
###      08_抽取操作ClickHouse的工具类.mp4
###      09_给问号占位符赋值.mp4
###      10_自定义注解标记不需要保存到CK的属性.mp4
###      11_构造者设计模式.mp4
###      12_商品统计思路分析.mp4
###      13_从Kafka中读取商品主题相关数据.mp4
###      14_封装商品统计实体类.mp4
###      15_商品点击行为的处理.mp4
###      16_商品曝光行为的处理.mp4
###      17_商品收藏行为的处理.mp4
### 
## day11
###      00_内容回顾.mp4
###      01_商品加购行为的处理.mp4
###      02_商品下单行为的处理.mp4
###      03_商品支付、退单、评论行为的处理.mp4
###      04_对合并结果进行测试.mp4
###      05_分组、开窗、聚合操作.mp4
###      06_支付回调时间提前生成现象解释.mp4
###      07_和商品维度进行关联.mp4
###      08_商品统计整体测试.mp4
###      09_地区主题统计需求分析.mp4
###      10_从Kafka主题中读取数据创建动态表.mp4
###      11_指定Watermark以及提取事件时间字段.mp4
###      12_分组、聚合.mp4
###      13_开窗操作.mp4
###      14_地区主题统计测试.mp4
###      15_关键词统计需求说明.mp4
###      16_封装IK分词工具类.mp4
###      17_自定义UDTF函数.mp4
###      18_从kafka中读取数据创建动态表.mp4
###      19_过滤数据以及分词并和原表字段连接.mp4
###      20_关键词统计测试.mp4
### 
## day12
###      00_内容回顾.mp4
###      01_大屏展示需求介绍.mp4
###      02_Sugar可视化组件介绍.mp4
###      03_商品总交易额思路分析.mp4
###      04_项目整体架构搭建.mp4
###      05_总交易额mapper接口定义.mp4
###      06_总交易额service代码实现.mp4
###      07_总交易额controller代码实现.mp4
###      08_总交易额大屏展示以及内网穿透.mp4
###      09_品牌交易额排名思路分析.mp4
###      10_品牌交易额排名Mapper层实现.mp4
###      11_上午内容回顾.mp4
###      12_品牌交易额controller实现以及大屏展示.mp4
###      13_品牌交易额占比整体实现.mp4
###      14_宏变量定义以及面向对象的方式处理返回的json.mp4
###      15_SPU交易额思路分析.mp4
###      16_SPU交易额代码实现.mp4
###      17_地区交易额思路分析.mp4
###      18_地区交易额代码实现以及大屏展示.mp4
###      
### 
## day13
###       00_新老访客指标对比思路分析.mp4
###       01_新老访客指标对比代码实现.mp4
###       02_访客指标分时统计思路分析.mp4
###       03_访客指标分时统计代码实现.mp4
###       04_关键词统计整体实现.mp4
###       05_关键词统计实时展示流程分析.mp4
###       06_关键词统计实时展示演示.mp4
###       07_打包到服务器运行流程.mp4
###       08_实时数仓项目回顾.mp4
###       09_Fink以及面试总结_基本理论.mp4
###       10_Fink以及面试总结_Watermark.mp4
###       11_Fink以及面试总结_keyby原理.mp4
###       12_Fink以及面试总结_资源分配.mp4
###       13_Fink以及面试总结_窗口.mp4
###       14_Fink以及面试总结_一致性以及反压.mp4
###       

```
总交易额
	-组件
		数字翻牌器

	-接口访问地址
		http://localhost:8070/api/sugar/gmv?date=20220325
		
	-获取数据的格式
		{
		  "status": 0,
		  "data": 1201030.6787633777
		}
	-SQL
		select sum(order_amount)  order_amount from product_stats_0906 where toYYYYMMDD(stt)=20220324;
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
品牌销售排行
	-组件
		横向柱状图

	-接口访问地址
		http://localhost:8070/api/sugar/tm?date=20220325&limit=5
		
	-获取数据的格式
		{
		  "status": 0,
		  "data": {
		    "categories": [
		      "苹果",
		      "三星",
		      "华为",
		      "oppo",
		      "vivo",
		      "小米86"
		    ],
		    "series": [
		      {
		        "name": "商品品牌",
		        "data": [
		          5845,
		          5440,
		          6042,
		          9898,
		          9498,
		          9206
		        ]
		      }
		    ]
		  }
		}
	-SQL
		select tm_id,tm_name,sum(order_amount) order_amount from product_stats_0906 where toYYYYMMDD(stt)=20220324 group by tm_id,tm_name having order_amount > 0 order by order_amount desc limit 5;

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
品类交易额占比
	-组件
		环形饼图

	-接口访问地址
		http://localhost:8070/api/sugar/category3?date=20220325&limit=5
		
	-获取数据的格式
		{
		  "status": 0,
		  "data": [
		    {
		      "name": "PC",
		      "value": 97
		    },
		    {
		      "name": "iOS",
		      "value": 50
		    },
		    {
		      "name": "Android",
		      "value": 59
		    }
		  ]
		}
	-SQL
		select category3_id,category3_name,sum(order_amount) order_amount from product_stats_0906 where toYYYYMMDD(stt)=20220324 group by category3_id,category3_name having order_amount > 0 order by order_amount desc limit 5;
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
热门商品交易额排名(SPU分组排名)
	-组件
		轮播表格

	-接口访问地址
		http://localhost:8070/api/sugar/spu?date=20220325&limit=5
		
	-获取数据的格式
		{
		  "status": 0,
		  "data": {
		    "columns": [
		      {
		        "name": "商品名称",
		        "id": "name"
		      },
		      {
		        "name": "交易额",
		        "id": "amount"
		      },
		      {
		        "name": "订单数",
		        "id": "ct"
		      }
		    ],
		    "rows": [
		      {
		        "name": "北京总部",
		        "amount": 500,
		        "ct": 50
		      },
		      {
		        "name": "北京总部",
		        "amount": 500,
		        "ct": 50
		      }
		    ]
		  }
		}
	-SQL
		select spu_id,spu_name,sum(order_amount) order_amount,sum(order_ct) order_ct from product_stats_0906 where toYYYYMMDD(stt)=20220324 group by spu_id,spu_name having order_amount > 0 order by order_amount desc limit 5;
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
不同地区交易额展示
	-组件
		中国省份色彩图

	-接口访问地址
		http://localhost:8070/api/sugar/province?date=20220325
		
	-获取数据的格式
		{
		  "status": 0,
		  "data": {
		    "mapData": [
		      {
		        "name": "北京",
		        "value": 5555
		      },
		      {
		        "name": "天津",
		        "value": 9282
		      }
		    ],
		    "valueName": "交易额"
		  }
		}
	-SQL
		select province_id,province_name,sum(order_amount) order_amount from province_stats_0906 where toYYYYMMDD(stt)=20220324 group by province_id,province_name;

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
新老用户访问指标对比
	-组件
		表格

	-接口访问地址
		http://localhost:8070/api/sugar/isnew?date=20220325
		
	-获取数据的格式
		{
		  "status": 0,
		  "data": {
		    "total": 5,
		    "columns": [
		      {
		        "name": "类别",
		        "id": "type"
		      },
		      {
		        "name": "新用户",
		        "id": "new"
		      },
		      {
		        "name": "老用户",
		        "id": "old"
		      }
		    ],
		    "rows": [
		      {
		        "type": "用户数(人)",
		        "new": 50,
		        "old": 200
		      },
		      {
		        "type": "总访问页面(次)",
		        "new": 50,
		        "old": 200
		      },
		      {
		        "type": "跳出率(%)",
		        "new": 50,
		        "old": 200
		      },
		      {
		        "type": "平均在线时长(秒)",
		        "new": 50,
		        "old": 200
		      },
		      {
		        "type": "平均访问页面数(人次)",
		        "new": 50,
		        "old": 200
		      }
		    ]
		  }
		}
	-SQL
	select is_new,sum(uv_ct) uv_ct,sum(pv_ct) pv_ct,sum(sv_ct) sv_ct,sum(uj_ct) uj_ct,sum(dur_sum) dur_sum from visitor_stats_0906 where toYYYYMMDD(stt)=20220324 group by is_new;
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
访客指标分时统计对比
	-组件
		折线图

	-接口访问地址
		http://localhost:8070/api/sugar/hr?date=20220325
		
	-获取数据的格式
		{
		  "status": 0,
		  "data": {
		    "categories": [
		      "00",
		      "01",
		      "...23"
		    ],
		    "series": [
		      {
		        "name": "pv",
		        "data": [
		          3622,
		          1459,
		          ...4949
		        ]
		      },
		      {
		        "name": "uv",
		        "data": [
		          5370,
		          2152,
		          ..12615
		        ]
		      },
		      {
		        "name": "new_uv",
		        "data": [
		          9849,
		          10770,
		          ...7186
		        ]
		      }
		    ]
		  }
		}
	-SQL
		select toHour(stt) hr,sum(visitor_stats_0906.uv_ct) uv_ct,sum(pv_ct) pv_ct,sum(if(is_new='1',visitor_stats_0906.uv_ct,0)) new_uv from visitor_stats_0906 where toYYYYMMDD(stt)=20220324 group by hr;

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
热度关键词
	-组件
		3D词云

	-接口访问地址
		http://localhost:8070/api/sugar/keyword?date=20220325
		
	-获取数据的格式
		{
		  "status": 0,
		  "data": [
		    {
		      "name": "海门",
		      "value": 1
		    },
		    {
		      "name": "鄂尔多斯",
		      "value": 1
		    }
		  ]
		}
	-SQL
		select keyword,  
			sum(keyword_stats_0906.ct * 
				multiIf(
					source='SEARCH',10,
					source='ORDER',5,
					source='CART',2,
					source='CLICK',1,0
				)) ct  
		from keyword_stats_0906 where toYYYYMMDD(stt)=20220324 group by keyword;

```



###       面试.mkv

1.   我就是能做, 学过的知识可以分析出来
2.   说细节, 说优化过程. 最好自己先做出来, 这样不用记忆. 
