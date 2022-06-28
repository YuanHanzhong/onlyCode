package realtime.app.dim;//package realtime.app.dim;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.JSONValidator;
//import com.ververica.cdc.connectors.mysql.source.MySqlSource;
//import com.ververica.cdc.connectors.mysql.table.StartupOptions;
//import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
//import org.apache.flink.api.common.eventtime.WatermarkStrategy;
//import org.apache.flink.api.common.functions.FilterFunction;
//import org.apache.flink.api.common.state.MapStateDescriptor;
//import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
//import org.apache.flink.streaming.api.datastream.BroadcastStream;
//import org.apache.flink.streaming.api.datastream.DataStreamSource;
//import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
//import realtime.app.func.DimSinkFunction;
//import realtime.app.func.TableProcessFunction;
//import realtime.bean.TableProcess;
//import realtime.utils.MyKafkaUtil;
//
////TODO 1.基本环境准备
////1.1 指定流处理环境
////1.2 设置并行度
//
////TODO 2.检查点相关的设置
////2.1 开启检查点
////2.2 检查点超时时间
////2.3 job取消之后，检查点是否保留
////2.4 两个检查点之间最小时间间隔
////2.5 设置重启策略
////2.6 设置状态后端
//
////TODO 3.从Kafka的topic_db中读取业务数据
//
////TODO 4.对读取的数据进行类型转换       jsonstr->jsonObj
//
////TODO 5.对读取的数据进行简单的ETL-----主流
//
////TODO 6.使用FlinkCDC读取配置表数据---配置流
//
////TODO 7.将配置流进行广播  ---广播流
//
////TODO 8.将主流和广播流关联在一起  ---connect
//
////TODO 9.分别对两条流进行处理---process    从主流中将维度数据过滤出来
//
////TODO 10.将维度数据写到phoenix表中
