package realtime.app.dim;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
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
import realtime.app.func.TableProcessFunction;
import realtime.bean.TableProcess;
import realtime.utils.MyKafkaUtil;

/**
 * Author: Felix
 * Date: 2022/6/29
 * Desc: 维度处理
 * 需要启动的进程
 * zk、kafka、maxwell、hdfs、hbase、DimApp
 * 执行流程
 * 运行模拟生成业务数据的jar包
 * 将生成的业务数据保存到业务数据库中
 * binlog会记录业务数据库表的变化
 * maxwell从binlog中读取变化数据并且将变化数据封装为json格式字符串发送给kafka的topic_db主题
 * DimApp从topic_db主题读取数据进行输出
 */
public class DimApp {
    public static void main(String[] args) throws Exception {
    
        //TODO 1.基本环境准备
        //1.1 指定流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);

        /*//TODO 2.检查点相关的设置
        //2.1 开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        //2.2 检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //2.3 job取消之后，检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //2.4 两个检查点之间最小时间间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);
        //2.5 设置重启策略
        //env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000L));
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30), Time.seconds(3)));
        //2.6 设置状态后端
        env.setStateBackend(new HashMapStateBackend());
        //env.getCheckpointConfig().setCheckpointStorage(new JobManagerCheckpointStorage());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/gmall/ck");

        //2.7 设置操作hdfs的用户
        System.setProperty("HADOOP_USER_NAME","atguigu");*/
    
        //TODO 3.从Kafka的topic_db中读取业务数据
        //3.1 声明消费的主题以及消费者组
        String topic = "topic_db";
        String groupId = "dim_sink_group";
    
        //3.2 创建消费者对象
        FlinkKafkaConsumer<String> kafkaConsumer = MyKafkaUtil.getKafkaConsumer(topic, groupId);
    
        //3.3 消费数据 封装为流
        DataStreamSource<String> kafkaStrDS = env.addSource(kafkaConsumer);
    
        //TODO 4.对读取的数据进行类型转换       jsonstr->jsonObj
        /*//匿名内部类
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(
            new MapFunction<String, JSONObject>() {
                @Override
                public JSONObject map(String jsonStr) throws Exception {
                    return JSON.parseObject(jsonStr);
                }
            }
        );
        //lambda表达式
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(
            jsonStr -> JSON.parseObject(jsonStr)
        );*/
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaStrDS.map(JSON::parseObject);
    
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
        //dimDS.addSink(new DimSinkFunction());
    
        env.execute();
    }
}
