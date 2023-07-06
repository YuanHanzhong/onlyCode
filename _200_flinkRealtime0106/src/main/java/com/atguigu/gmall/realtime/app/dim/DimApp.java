package com.atguigu.gmall.realtime.app.dim;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.app.func.DimSinkFunction;
import com.atguigu.gmall.realtime.app.func.TableProcessFunction;
import com.atguigu.gmall.realtime.bean.TableProcess;
import com.atguigu.gmall.realtime.util.MyKafkaUtil;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

/**
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
 *      运行DimApp应用的时候会从配置表中加载配置信息
 *      根据配置信息提前创建维度表
 *      将配置信息放到广播状态中
 *      运行模拟生成业务数据的jar包
 *      将生成的业务数据保存到业务数据库中
 *      binlog会记录业务数据库表的变化
 *      maxwell从binlog中读取变化数据并且将变化数据封装为json格式字符串发送给kafka的topic_db主题
 *      DimApp从topic_db主题读取数据
 *      根据当前读取数据的表名到广播状态中找对应的配置
 *          如果找到：是维度
 *          如果没有找到：不是维度
 *      是维度的话，将维度数据输出到下游
 *      将下游维度流写到phoenix表中
 */
public class DimApp {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        
        // STAR
        /*
        * FAS
        * */
        
        /*//TODO 2.检查点相关设置
        //2.1 开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        //2.2 设置检查点的超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.3 取消job后检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //2.4 设置两个检查点之间最小时间间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);
        //2.5 设置重启策略
        //env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000L));
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30), Time.seconds(3)));
        //2.6 设置状态后端
        env.setStateBackend(new HashMapStateBackend());
        //env.getCheckpointConfig().setCheckpointStorage(new JobManagerCheckpointStorage());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/gmall/ck");
        //2.7 指定操作hadoop的用户
        System.setProperty("HADOOP_USER_NAME","atguigu");*/

        //TODO 3.从kafka的topic_db主题中读取业务数据
        //3.1 声明消费的主题以及消费者组
        /*
        *
        *
        *
        *
        *
        *
        *
        * */
        String topic = "topic_db";
        String groupId = "dim_app_group";
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);

        //TODO 4.对读取的数据格式进行转换       jsonStr->jsonObj
        /*//匿名内部类方式
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(
            new MapFunction<String, JSONObject>() {
                @Override
                public JSONObject map(String jsonStr) throws Exception {
                    return JSONObject.parseObject(jsonStr);
                }
            });
        //lambda表达式方式
        SingleOutputStreamOperator<JSONObject> jsonObjDS1 = kafkaStrDS.map(jsonStr -> JSON.parseObject(jsonStr));
        */
        //方法的默认调用
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);

        //TODO 5.对读取的主流业务数据进行简单的ETL
        SingleOutputStreamOperator<JSONObject> filterDS = jsonObjDS.filter(
            new FilterFunction<JSONObject>() {
                @Override
                public boolean filter(JSONObject jsonObj) throws Exception {
                    try {
                        jsonObj.getJSONObject("data");
                        if (jsonObj.getString("type").equals("bootstrap-start")
                            || jsonObj.getString("type").equals("bootstrap-complete")) {
                            return false;
                        }
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
        );

        //filterDS.print(">>>>");

        //TODO 6.使用FlinkCDC读取配置表数据--配置流
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
            .hostname("hadoop102")
            .port(3306)
            .databaseList("GMALL2022_config")
            .tableList("GMALL2022_config.table_process")
            .username("root")
            .password("root")
            .deserializer(new JsonDebeziumDeserializationSchema())
            .startupOptions(StartupOptions.initial())
            .build();

        DataStreamSource<String> mySqlDS
            = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source");

        mySqlDS.print("6.使用FlinkCDC读取配置表数据--配置流>>>>>");

        //TODO 7.将配置流进行广播--广播流
        MapStateDescriptor<String, TableProcess> mapStateDescriptor
            = new MapStateDescriptor<String, TableProcess>("mapStateDescriptor",String.class,TableProcess.class);
        BroadcastStream<String> broadcastDS = mySqlDS.broadcast(mapStateDescriptor);

        
        
        //TODO 8.将业务主流和配置广播流进行关联--connect
        BroadcastConnectedStream<JSONObject, String> connectDS = filterDS.connect(broadcastDS);

        //TODO 9.对关联之后的数据进行处理--process 从业务流中将维度数据过滤出来
        SingleOutputStreamOperator<JSONObject> dimDS = connectDS.process(
            new TableProcessFunction(mapStateDescriptor)
        );
        //TODO 10.将处理得到维度数据写到phoenix表中
        dimDS.print("10.将处理得到维度数据写到phoenix表中>>>>");
        dimDS.addSink(new DimSinkFunction());
        env.execute();
    }
}
