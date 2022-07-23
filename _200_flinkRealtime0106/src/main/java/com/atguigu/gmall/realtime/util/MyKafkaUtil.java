package com.atguigu.gmall.realtime.util;

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

/**
 * Author: Felix
 * Date: 2022/6/29
 * Desc: 操作kafka的工具类
 */
public class MyKafkaUtil {
    public static final String KAFKA_SERVER = "hadoop102:9092,hadoop103:9092,hadoop104:9092";

    //获取消费者对象
    public static FlinkKafkaConsumer<String> getKafkaConsumer(String topic, String groupId) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        //注意：这种方式使用SimpleStringSchema进行反序列化的时候，如果kafka主题中消息为null，会报错
        //FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<String>(topic,new SimpleStringSchema(),props);
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<String>(topic, new KafkaDeserializationSchema<String>() {
            @Override
            public boolean isEndOfStream(String nextElement) {
                return false;
            }

            @Override
            public String deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
                if (record != null && record.value() != null) {
                    return new String(record.value());
                }
                return null;
            }

            @Override
            public TypeInformation<String> getProducedType() {
                return TypeInformation.of(String.class);
            }
        }, props);
        return kafkaConsumer;
    }

    //获取生产者对象
    public static FlinkKafkaProducer<String> getKafkaProducer(String topic) {
        //注意：通过以下方式创建的FlinkKafkaProducer对象默认Semantic的值是AT_LEAST_ONCE，不能保证精准一次性
        //FlinkKafkaProducer kafkaProducer = new FlinkKafkaProducer("hadoop102:9092","dirty_data",new SimpleStringSchema());
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 15 * 60 * 1000 + "");
        FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<String>("default_topic",
            new KafkaSerializationSchema<String>() {
                @Override
                public ProducerRecord<byte[], byte[]> serialize(String jsonStr, @Nullable Long timestamp) {
                    return new ProducerRecord<byte[], byte[]>(topic, jsonStr.getBytes());
                }
            }, props, FlinkKafkaProducer.Semantic.EXACTLY_ONCE);
        return kafkaProducer;
    }

    //获取kafka连接器相关的连接属性
    public static String getKafkaDDL(String topic, String groupId) {
        return "WITH (\n" +
            "  'connector' = 'kafka',\n" +
            "  'topic' = '" + topic + "',\n" +
            "  'properties.bootstrap.servers' = '" + KAFKA_SERVER + "',\n" +
            "  'properties.group.id' = '" + groupId + "',\n" +
            "  'scan.startup.mode' = 'group-offsets',\n" +
            "  'format' = 'json'\n" +
            ") ";
    }

    //获取upsert-kakfka连接器相关的连接属性
    public static String getUpsertKafkaDDL(String topic) {
        return " WITH (\n" +
            "  'connector' = 'upsert-kafka',\n" +
            "  'topic' = '" + topic + "',\n" +
            "  'properties.bootstrap.servers' = '" + KAFKA_SERVER + "',\n" +
            "  'key.format' = 'json',\n" +
            "  'value.format' = 'json'\n" +
            ")";
    }

    //获取从kafka的topic_db中读取数据创建动态表的建表语句
    public static String getTopicDbDDL(String groupId){
        return "create table topic_db(\n" +
            " `database` string,\n" +
            " `table` string,\n" +
            " `type` string,\n" +
            " `ts` string,\n" +
            " `old` map<string,string>,\n" +
            " `data` map<string,string>,\n" +
            " `proc_time` as proctime()\n" +
            ") " + getKafkaDDL("topic_db",groupId);
    }
}
