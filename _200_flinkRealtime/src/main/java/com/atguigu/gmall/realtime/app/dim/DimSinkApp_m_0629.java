package com.atguigu.gmall.realtime.app.dim;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.atguigu.gmall.realtime.app.func.DimSinkFunction;
import com.atguigu.gmall.realtime.app.func.TableProcessFunction;
import com.atguigu.gmall.realtime.bean.TableProcess;
import com.atguigu.gmall.realtime.common.GmallConfig_m;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import com.atguigu.gmall.realtime.util.MyKafkaUtil_m_0712;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

/*
 * Author: Felix
 * Date: 2022/6/29
 * Desc: 维度处理
 * 需要启动的进程
 *      zk、kafka、maxwell、hdfs、hbase、DimApp
 * 开发流程
 *      基本环境准备
 *          -指定流处理环境
 *          -设置并行度
 *      检查点设置
 *          -开启检查点
 *          -设置检查点超时时间
 *          -设置取消job后检查点是否保留
 *          -设置两个检查点之间最小时间间隔
 *          -设置重启策略
 *          -设置状态后端
 *          -设置操作hadoop的用户
 *      从kafka的topic_db主题中读取数据
 *          -声明消费的主题以及消费者组
 *          -创建消费者对象 FlinkKafkaConsumer
 *          -消费数据 封装为流
 *      对读取的数据进行类型的转换   jsonStr->jsonObj
 *      对业务流数据进行简单的ETL
 *      ------------------------以上为业务数据的读取--------------------------
 *      使用FlinkCDC读取配置表数据---配置流
 *      将配置流进行广播---广播流
 *      ------------------------以上为配置数据的读取--------------------------
 *      将业务流和广播流进行关联---connect
 *      对关联之后的数据进行处理---process
 *      在处理关联之后的数据的时候，我们这里封装一个TableProcessFunction
 *          -processBroadcastElement  处理广播流数据
 *              从广播流获取flinkCDC读取的配置信息
 *              判断操作类型
 *                  d--删除 ：从状态中删除配置
 *                  c/u:添加或者修改 ：将配置添加或者更新到状态中
 *              在向状态中添加数据之前，提前将维度表创建了出来
 *          -processElement  处理业务流数据
 *              获取广播状态
 *              根据当前处理的业务流中数据的表名到广播状态中获取对应的配置信息
 *              如果获取到了，说明是维度，将维度数据继续向下游传递
 *                  过滤不需要传递的字段
 *                  补充sink_table属性
 *              如果没有获取到，说明不是维度，什么也不做，相当于过滤掉了
 *      将流中的维度数据，写到Phoenix表中
 *          封装了DimSinkFunction---invoke
 *              拼接upsert语句
 *              抽取PhoenixUtil工具类，执行SQL
 *              简化建表语句执行过程
 *      将历史数据同步到phoenix表中
 *
 * 执行流程
 *      运行模拟生成业务数据的jar包
 *      将生成的业务数据保存到业务数据库中
 *      binlog会记录业务数据库表的变化
 *      maxwell从binlog中读取变化数据并且将变化数据封装为json格式字符串发送给kafka的topic_db主题
 *      DimApp从topic_db主题读取数据进行输出
 */
public class DimSinkApp_m_0629 {
    // 2022/7/10 19:39 NOTE package 之后报很多找不到包的错误, 就是因为引入了一个不必要的包
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);

        // STAR 添加以来的时候, 放到最后比较好, 避免改变原来的依赖结构

        //2.1 开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        //2.5 设置重启策略
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30), Time.seconds(3)));


        //存储
        //2.6 设置状态后端, 即主要设置本地状态保存在哪里
        /*
         2022/7/13 20:24 状态后端
         区别
            hash, **本地状态**完全在内存中, 快而小
            rocksdb, **本地状态**完全在在job mananger的文件系统中(固态或磁盘), 慢而大
        */
        
        
        //env.setStateBackend(new EmbeddedRocksDBStateBackend()); // 使用Rocksdb, 需要引入相关依赖
        env.setStateBackend(new HashMapStateBackend());
        
        /*
         2022/7/13 20:59 设置持久化存储位置, 速度
        */
        
        env.getCheckpointConfig().setCheckpointStorage(GmallConfig_m.CHECK_POINT_STORAGE);
        System.setProperty("HADOOP_USER_NAME","atguigu");
        //2.3 job取消之后，检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        //2.2 检查点超时时间, 60s
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.4 两个检查点之间最小时间间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);

        
        //TODO 3.从Kafka的topic_db中读取业务数据
        //3.1 声明消费的主题以及消费者组
        String topic = "topic_db";
        String groupId = "dim_sink_group";

        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);

        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
        

        //TODO 4.对读取的数据进行类型转换       jsonstr->jsonObj
        
        //匿名内部类
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(
            new MapFunction<String, JSONObject>() {
                @Override
                public JSONObject map(String jsonStr) throws Exception {
                    return JSON.parseObject(jsonStr);
                }
            }
        );
        
        //lambda表达式
        //SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(
        //    jsonStr -> JSON.parseObject(jsonStr)
        //);
        

        // 匿名内部类的方式比较好, 其他的容易类型擦除
        //SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //TODO 5.对读取的数据进行简单的ETL-----主流
        SingleOutputStreamOperator<JSONObject> filterDS = jsonObjDS.filter(
            new FilterFunction<JSONObject>() {
                @Override
                public boolean filter(JSONObject jsonObj) throws Exception {
                    try {
                        String dataJsonStr = jsonObj.getString("data");
                        JSONValidator.from(dataJsonStr).validate();
                        if(jsonObj.getString("type").equals("bootstrap-start")||jsonObj.getString("type").equals("bootstrap-complete")){
                            return  false;
                        }
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
        );
        filterDS.print(">>>>");
        //
        //TODO 6.使用FlinkCDC读取配置表数据---配置流
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
            .hostname("hadoop102")
            .port(3306)
            .databaseList("gmall2022_config") // set captured database
            .tableList("gmall2022_config.table_process") // set captured table
            .username("root")
            .password("root")
            .startupOptions(StartupOptions.initial())
            .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
            .build();


        DataStreamSource<String> mySQLDS = env
            .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source");
        mySQLDS.print(">>>>");

        //TODO 7.将配置流进行广播  ---广播流
        MapStateDescriptor<String, TableProcess> mapStateDescriptor
            = new MapStateDescriptor<>("mapStateDescriptor", String.class, TableProcess.class);
        BroadcastStream<String> broadcastDS = mySQLDS.broadcast(mapStateDescriptor);

        //TODO 8.将主流和广播流关联在一起  ---connect
        BroadcastConnectedStream<JSONObject, String> connectDS = filterDS.connect(broadcastDS);

        //TODO 9.分别对两条流进行处理---process    从主流中将维度数据过滤出来
        SingleOutputStreamOperator<JSONObject> dimDS = connectDS.process(
            new TableProcessFunction(mapStateDescriptor)
        );

        dimDS.print(">>>>");

        //TODO 10.将维度数据写到phoenix表中
        dimDS.addSink(new DimSinkFunction());

        env.execute();
    }
}
