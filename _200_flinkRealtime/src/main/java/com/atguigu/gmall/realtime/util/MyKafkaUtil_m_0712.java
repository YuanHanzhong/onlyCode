package com.atguigu.gmall.realtime.util;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.Nullable;
import java.util.Properties;
    /*
     2022/7/12 17:58 STAR 常用的东西抽象出来, 不管是函数还是字符串.
    */


/**
 * STAR Flink和Kafka交互的时候, 只需要使用FlinkKafkaConsumer就好
 * 1. 使用kafka原生的API实现精准一次.
 * 1.1 先处理, 再手动提交.
 * 1.2 把处理和提交绑定为事物
 * 2. FlinkafkaConsumer已经实现以上功能直接用就好
 */
    
    /*
     2022/7/12 18:02 NOTE STAR
     消费kafka数据, 两个必要
        1. 声明topic
        2. 声明消费者组
    */
    /*
     2022/7/12 18:08 NOTE
     Flink 状态主要2种
        1. 键控状态
        2. 算子状态
            2.1 广播
            2.2 List
                2.2.1 ValueState
                2.2.2 ListState
                2.2.3 MapState
                
    */
    
    /*
     2022/7/12 18:52 NOTE
     STAR 类的命名规则: 动词加名词
     
    */
    
    /*
     2022/7/12 18:57 NOTE SimpleStringSchema,
     1. Flink专门提供的
     2. 对字符串进行序列化和反序列的类
     3. 致命的问题就是不能为null,
    */
    
    /*
     2022/7/15 20:08 NOTE
     kafka生产者生产数据一致性, 需要3个条件
        ack = -1,
        开启幂等性
        开启事物
    */
    /*
     2022/7/15 20:35 两阶段提交 NOTE
        预提交
        检查点做好后才真正提交
    */
    /*
     2022/7/15 20:44 NOTE 往kafka写数据的时候的分区策略
     
    */

public class MyKafkaUtil_m_0712 {
    private static final String KAFKA_SERVER = "hadoop102:9092,hadoop103:9092,hadoop104:9092";
    
    // 2022/7/15 20:32 NOTE 实现kafkaConsumer
    public static FlinkKafkaConsumer<String> getKafkaConsumer(String topic, String groupId) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        
        return new FlinkKafkaConsumer<String>( // 消费者是带有Flink的消费者 NOTE // 2022/7/15 19:03
          topic,
          new KafkaDeserializationSchema<String>() {
              @Override
              public TypeInformation<String> getProducedType() {
                  return TypeInformation.of(String.class);
              }
              
              @Override
              public boolean isEndOfStream(String nextElement) {
                  return false;
              }
              
              @Override
              public String deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
                  if (record != null && record.value()!=null) { // 2022/7/15 19:03  NOTE 用 . 的时候, 也要先判断下, null
                    return new String(record.value()); // 2022/7/15 19:03  new String时有必要判断下, 里面的不能为null
                  }
                  
                  return null;
              }
              
          }, // 2022/7/15 19:03  NOTE 反序列化, 是kafka相关的反序列花
          props);
    }
    
    //获取生产者对象的方法
    public static FlinkKafkaProducer<String> getKafkaProducer1(String topic) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 15 * 60 * 1000 + "");// 2022/7/15 20:39 NOTE Properties里的都是字符串
        // 2022/7/15 20:38 NOTE 事物的最大时间是15分钟,
        // 2022/7/15 20:35 kafkaProducer , 两阶段提交
        /*
         2022/7/15 20:37  NOTE 检查点的提交包含在事物里,             所以事物的时间要设置的比检查点的时间大
        */
        
    
        KafkaSerializationSchema<String> kafkaSerializationSchema = new KafkaSerializationSchema<String>() {
            // 2022/7/15 20:46 NOTE ProducerRecord 里可以指定分区策略
            // NOTE 看类的话, 点击前面的, 看方法的话, 点击后面的. 一般点击前面的.
    
            @Override
            public ProducerRecord<byte[], byte[]> serialize(String element, @Nullable Long timestamp) {
                return new ProducerRecord<byte[], byte[]>(topic,element.getBytes()) ; // 2022/7/15 20:42 NOTE return new
                // 2022/7/15 20:41 NOTE 所谓的序列化, 就是转化为 byte[]
            }
        };
    
        return new FlinkKafkaProducer<String>(topic,
          new SimpleStringSchema(),
          // 2022/7/15 20:05 NOTE 序列化和反序列化方法是不同的
          props);
    }
    
    public static FlinkKafkaProducer<String> getKafkaProducer(String topic) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 15 * 60 * 1000 + "");
    
        FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<>(
          "default_topic",
          new KafkaSerializationSchema<String>() {
            @Override
            public ProducerRecord<byte[], byte[]> serialize(String element, @Nullable Long timestamp) {
                return new ProducerRecord<byte[], byte[]>(topic, element.getBytes());
            }
        },
          props,
          FlinkKafkaProducer.Semantic.EXACTLY_ONCE);
        return kafkaProducer;
    }
    
    
    //获取kafka连接器相关连接属性
        public static String getKafkaDDL (String topic, String groupId){
            return "WITH (\n" +
                     "  'connector' = 'kafka',\n" +
                     "  'topic' = '" + topic + "',\n" +
                     "  'properties.bootstrap.servers' = '" + KAFKA_SERVER + "',\n" +
                     "  'properties.group.id' = '" + groupId + "',\n" +
                     "  'scan.startup.mode' = 'group-offsets',\n" +
                     "  'format' = 'json'\n" +
                     ")";
        }
        
        //获取upsert-kafka连接器相关连接属性
        public static String getUpsertKafkaDDL (String topic){
            return "WITH (\n" +
                     "  'connector' = 'upsert-kafka',\n" +
                     "  'topic' = '" + topic + "',\n" +
                     "  'properties.bootstrap.servers' = '" + KAFKA_SERVER + "',\n" +
                     "  'key.format' = 'json',\n" +
                     "  'value.format' = 'json'\n" +
                     ")";
        }
        
        //获取从kafka的topic_db主题中读取数据创建动态表的DDL
        public static String getTopicDbDDL (String groupId){
            return "create table topic_db(" +
                     "`database` String,\n" +
                     "`table` String,\n" +
                     "`type` String,\n" +
                     "`data` map<String, String>,\n" +
                     "`old` map<String, String>,\n" +
                     "`proc_time` as PROCTIME(),\n" +
                     "`ts` string\n" +
                     ")" + getKafkaDDL("topic_db", groupId);
        }
    }
