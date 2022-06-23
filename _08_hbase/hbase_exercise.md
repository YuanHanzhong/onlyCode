1. HBase shell namespace
    1) 查看所有的namespace
       ```sql
       list_namespace
    2) 创建namespace
       ```sql
       create_namespace 'mydb'
       create_namespace 'mydb1' , {'createtime'=>'2022-05-31' , 'author'=>'atguigu'}
    3) 查看namespace的详情
       ```sql
       describe_namespace 'mydb'
    4) 修改namespace
       ```sql
       alter_namespace 'mydb1' , {METHOD=>'set' , 'author'=>'wyh' }
       alter_namespace 'mydb1' , {METHOD=>'set' , 'addr'=>'beijing'}
       alter_namespace 'mydb1' , {METHOD=>'set' , 'author'=>'wyh' ,'addr'=>'beijing'}
       alter_namespace 'mydb1' , {METHOD=>'unset' , NAME=>'addr' }
    5) 删除namespace(只能删除空的namespace)
       ```sql
       drop_namespace 'mydb'
    6) 查看namespace下的表
       ```sql
       list_namespace_tables 'mydb1'

2. HBase shell table ddl

    1) 查看所有的表
       ```sql
       list

    2) 创建表
       ```sql
       create 'test1', {NAME=>'info1' , VERSIONS=>'3'} , {NAME=>'info2'}
       create 'mydb1:test2' , {NAME=>'info1'}
       create 'test3' , 'info1' , 'info2'

    3) 查看表的详情
       ```sql
       describe 'test1'
       desc 'test1'

    4) 修改表
       ```sql
       alter 'test3' , NAME=> 'info1' , VERSIONS => 5
       alter 'test3' , NAME=> 'info3' , VERSIONS => 5
       alter 'test3',  NAME => 'info1', METHOD => 'delete'
       alter 'test3', 'delete' => 'info2'

    5) 表的状态
       ```sql
       is_enabled 'test3'
       is_disabled 'test3'
       disable 'test3'
       enable 'test3'

    6) 删除表
       ```sql
       disable 'test3'
       drop 'test3'

    7) 查看表是否存在
       ```sql
       exists 'test3'

    8) 查看表的regions
       ```sql
       list_regions 'test3'

3. HBase shell table dml

    1) 新增数据
       ```sql
       disable 'stu'
       drop 'stu'
       create 'stu' , 'info1' , 'info2'
       put 'stu' , '1001' , 'info1:name' , 'zhangsan'
       put 'stu' , '1001' , 'info1:age'  , '20'
       put 'stu' , '1001' , 'info1:gender' , 'man'
       put 'stu' , '1001' , 'info2:addr' , 'beijing'

       put 'stu' , '1002' , 'info1:name' , 'lisi'
       put 'stu' , '1002' , 'info1:age'  , '25'
       put 'stu' , '1002' , 'info1:gender' , 'woman'
       put 'stu' , '1002' , 'info2:addr' , 'shanghai'
       put 'stu' , '1002' , 'info2:passwd' , 'nicai'

       put 'stu' , '1003' , 'info1:name' , 'wangwu'
       put 'stu' , '1003' , 'info1:age'  , '30'
       put 'stu' , '1003' , 'info1:gender' , 'man'
       put 'stu' , '1003' , 'info2:addr' , 'tianjing'

       put 'stu' , '10021' , 'info1:name' , 'zhaoliu'
       put 'stu' , '10021' , 'info1:age'  , '35'
       put 'stu' , '10021' , 'info1:gender' , 'man'
       put 'stu' , '10021' , 'info2:addr' , 'hebei'

       put 'stu' , '10020' , 'info1:name' , 'tianqi'
       put 'stu' , '10020' , 'info1:age'  , '40'
       put 'stu' , '10020' , 'info1:gender' , 'women'
       put 'stu' , '10020' , 'info2:addr' , 'shanxi'


2) 基于rowkey查询数据
   ```sql
   get 'stu' , '1001'    
   get 'stu' , '1001' , 'info1:name'

3) 扫描数据
   ```sql
   scan 'stu'   
   scan 'stu' ,{STARTROW=>'1001'  , STOPROW=> '1002!'}
   scan 'stu' ,{STARTROW=>'1001'  , STOPROW=> '1002|'}
   scan 'stu' , {RAW=>true, VERSIONS=>5}

4) 修改数据
   ```sql
   put 'stu' , '1001' , 'info1:name' ,'zhangxiaosan'

5) 删除操作
   ```sql
   delete 'stu' , '1001' , 'info1:name'  删除指定版本的数据，默认最新版本(Delete)
   deleteall 'stu' , '1002' , 'info1:name' 删除一个列所有版本的数据(DeleteColumn)
   deleteall 'stu' , '1002' 删除整个列族的数据(DeleteFamily)

6) 统计表中的数据条数
   ```sql
   count 'stu'
7) 清空表数据
   ```sql
   truncate 'stu'  直接删除内存中对应的数据和HDFS中对应的数据文件


4. rowkey设计
    1) 数据
       user			date					pay
       zhangsanfeng	2022-01-04 09:08:00		200
       zhangsan 		2022-01-05 09:08:00		100
       zhangsan 		2021-12-31 09:08:00		150
       zhangsanfeng	2021-12-04 09:08:00		230
       lisi			2021-12-31 09:08:00		150
       zhangsan 		2021-12-30 09:08:00		100

   2)需求一: 统计张三在2021年12月份消费的总金额

   rowkey:  user_date(yyyy-MM-dd HH:mm:SS)

   ```sql
   create 'test1' , 'info'
   put  'test1' , 'zhangsan_2022-01-05 09:08:00' , 'info:pay' , 100
   put  'test1' , 'zhangsan_2021-12-30 09:08:00' , 'info:pay' , 100
   put  'test1' , 'zhangsan_2021-12-31 09:08:00' , 'info:pay' , 150
   put  'test1' , 'zhangsanfeng_2022-01-04 09:08:00' , 'info:pay' , 200
   put  'test1' , 'zhangsanfeng_2021-12-04 09:08:00' , 'info:pay' , 230
   put  'test1' , 'lisi_2021-12-31 09:08:00' , 'info:pay' , 150

   扫描数据: scan 'test1' , {STARTROW=>'zhangsan_2021-12'  ,STOPROW=>'zhangsan_2021-12|'}


3)需求二: 统计所有人在2021年12月份消费的总金额

     rowkey: date(yyyy-MM)_user_date(-dd HH:mm:SS)

   ```sql
     create 'test2' , 'info'
     put  'test2' , '2022-01_zhangsan_-05 09:08:00' , 'info:pay' , 100
     put  'test2' , '2021-12_zhangsan_-30 09:08:00' , 'info:pay' , 100
     put  'test2' , '2021-12_zhangsan_-31 09:08:00' , 'info:pay' , 150
     put  'test2' , '2022-01_zhangsanfeng_-04 09:08:00' , 'info:pay' , 200
     put  'test2' , '2021-12_zhangsanfeng_-04 09:08:00' , 'info:pay' , 230
     put  'test2' , '2021-12_lisi_-31 09:08:00' , 'info:pay' , 150

     扫描数据(需求二): scan 'test2' , {STARTROW=>'2021-12' , STOPROW=>'2021-12|'} 
     扫描数据(需求一): scan 'test2' , {STARTROW=>'2021-12_zhangsan_' , STOPROW=>'2021-12_zhangsan_|'}

4)预分区:  假定120个分区
000 ~ 001
001 ~ 002
002 ~ 003
........

     假设希望将某个人某月的数据存放到一个分区 
     计算分区号 : hash(user + yyyy-MM) % 120 = 0 - 119
     rowkey : 分区号_date(yyyy-MM)_user_date(-dd HH:mm:SS) 
     如上设计存在的问题数据太散列 ，如果从需求二的角度考虑，可能需要扫描所有的分区. 


     假定每个月份的数据会对应固定的10个分区
     000 - 009 号分区存储1月份数据
	 ...
     110 - 119 号分区存储12月份数据
     计算分区号: hash(user) % 10 +  月份对应的分区开始值
     rowkey: 分区号_date(yyyy-MM)_user_date(-dd HH:mm:SS)


5. Phoenix Shell
    1) 创建schema(库)
       create schema if not exists mydb;
       create schema if not exists "mydb2";

    2) 注意
       在phoenix中写的schema名、表名、字段名将来在Hbase中都会自动转换成大写形式.
       如果不想转换成大写，可以通过""引起来.

    3) 删除schema
       drop schema if exists "mydb2";

    4) 创建表
       create table emp(
       id varchar(20) primary key ,
       name varchar(20) ,
       addr varchar(20)
       );

    5) 插入数据
       upsert into emp (id, name, addr ) values ('1001', 'zhangsan' , 'beijing');
       upsert into emp (id, name, addr ) values ('1002', 'lisi' , 'shanghai');

    6) 查询数据
       select * from emp ;
       select id ,name ,addr from emp ;
       select id ,name ,addr from emp where id = '1001';

    7) 删除数据
       delete from emp where id = '1002';

    8) 删除表

       drop table emp ;

6. 使用Phoenix的几个问题:
    1) 约定
       如果使用了phoenix ,就遵循读和写都通过phoenix来完成, 或者读和写都通过Hbase完成。

    2) 列编码问题
       Phoenix在hbase中建表会使用编码.如果不想使用编码,可以在建表时进行指定。

       create table emp1(
       id varchar(20) primary key ,
       name varchar(20) ,
       addr varchar(20)
       )
       COLUMN_ENCODED_BYTES = NONE;

       upsert into emp1 (id, name, addr ) values ('1001', 'zhangsan' , 'beijing');

    3) value=x
       主要解决当一条数据只有主键没有别的字段的情况。
       upsert into emp1 (id, name, addr ) values ('1002', null , null);


7. 表映射
   create  table "stu"(
   id varchar(20) primary key ,
   "info1"."name" varchar(20) ,
   "info1"."gender" varchar(20) ,
   "info1"."age" varchar(20) ,
   "info2"."addr" varchar(20),
   "info2"."passwd" varchar(20)
   )
   COLUMN_ENCODED_BYTES = NONE ;

8. 全局索引
    0) 所谓的全局索引就是会创建一张索引表， 然后将索引字段与原表的rowkey拼接成新的rowkey，存储到索引表中。

    1) SQL分析工具: Explain

    2) 分析几条SQL
       create table emp(
       id varchar(20) primary key ,
       name varchar(20) ,
       addr varchar(20)
       )
       COLUMN_ENCODED_BYTES = NONE ;
       upsert into emp (id, name, addr ) values ('1001', 'zhangsan' , 'beijing');
       upsert into emp (id, name, addr ) values ('1002', 'lisi' , 'shanghai');

       explain select id  from emp ;  //FULL SCAN OVER EMP
       explain select id , name, addr from emp where id = '1001' ; //POINT LOOKUP ON 1 KEY OVER EMP
       explain select id , name from emp where name = 'zhangsan' ; //FULL SCAN OVER EMP

    3) 创建全局索引
       create index idx_emp_name on emp(name) ;    
       explain select id ,name  from emp where name = 'zhangsan' ; //RANGE SCAN OVER IDX_EMP_NAME ['zhangsan']

    4) 如果想在查询的字段列表中，出现非索引字段怎么办?
       explain select id ,name , addr  from emp where name = 'zhangsan' ;

       办法一:  将非索引字段一并建索引,建议复合索引。
       drop index idx_emp_name on emp ;
       create index idx_emp_name_addr on emp(name,addr) ;
       explain select id ,name , addr  from emp where name = 'zhangsan' ; //RANGE SCAN OVER IDX_EMP_NAME_ADDR ['zhangsan']

       办法二:  包含索引
       drop index idx_emp_name_addr on emp ;
       create index idx_emp_name_include_addr on emp(name) include(addr) ;
       explain select id ,name , addr  from emp where name = 'zhangsan' ; //RANGE SCAN OVER IDX_EMP_NAME_INCLUDE_ADDR ['zhangsan']

       办法三: 本地索引


9. 本地索引
    0) 所谓的本地索引就是将索引字段与原表的rowkey拼接成新的rowkey，插入到原表中。

    1) 创建本地索引
       drop index idx_emp_name_include_addr on emp ;
       create local index idx_emp_name on emp(name);   
       explain select id ,name , addr  from emp where name = 'zhangsan' ; // RANGE SCAN OVER EMP [1,'zhangsan']    
