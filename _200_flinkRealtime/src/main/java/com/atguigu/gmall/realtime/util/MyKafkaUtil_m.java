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

/*
2022/7/4 10:05 NOTE TODO KafkaUtils




    优化:
        SimpleStreamSchema,
            有null时会报错
            

 */
public class MyKafkaUtil_m {
    private static final String KAFKA_SERVER = "hadoop102:9092,hadoop103:9092,hadoop104:9092";

    //获取消费者对象的方法
    public static FlinkKafkaConsumer<String> getKafkaConsumer(String topic, String groupId) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        //FlinkKafkaConsumer kafkaConsumer = new FlinkKafkaConsumer(topic,new SimpleStringSchema(),props);
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(topic, new KafkaDeserializationSchema<String>() {
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

    //获取生产者对象的方法
    public static FlinkKafkaProducer<String> getKafkaProducer(String topic) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 15 * 60 * 1000 + "");

        FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<>("default_topic", new KafkaSerializationSchema<String>() {
            @Override
            public ProducerRecord<byte[], byte[]> serialize(String element, @Nullable Long timestamp) {
                return new ProducerRecord<byte[], byte[]>(topic, element.getBytes());
            }
        }, props, FlinkKafkaProducer.Semantic.EXACTLY_ONCE);
        return kafkaProducer;
    }

    //获取kafka连接器相关连接属性
    public static String getKafkaDDL(String topic, String groupId) {
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
    public static String getUpsertKafkaDDL(String topic) {
        return "WITH (\n" +
            "  'connector' = 'upsert-kafka',\n" +
            "  'topic' = '" + topic + "',\n" +
            "  'properties.bootstrap.servers' = '" + KAFKA_SERVER + "',\n" +
            "  'key.format' = 'json',\n" +
            "  'value.format' = 'json'\n" +
            ")";
    }

    //获取从kafka的topic_db主题中读取数据创建动态表的DDL
    public static String getTopicDbDDL(String groupId) {
        return "create table topic_db(" +
            "`database` String,\n" +
            "`table` String,\n" +
            "`type` String,\n" +
            "`data` map<String, String>,\n" +
            "`old` map<String, String>,\n" +
            "`proc_time` as PROCTIME(),\n" +
            "`ts` string\n" +
            ")" + getKafkaDDL("topic_db",groupId);
    }
}
