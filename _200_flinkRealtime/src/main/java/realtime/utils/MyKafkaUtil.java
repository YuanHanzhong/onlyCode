package realtime.utils;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Properties;

/**
 * Author: Felix
 * Date: 2022/5/17
 * Desc: 操作kafka的工具类
 */
public class MyKafkaUtil {
    private static  final String KAFKA_SERVER = "hadoop202:9092,hadoop203:9092,hadoop204:9092";
    //获取消费者对象的方法
    public static FlinkKafkaConsumer<String> getKafkaConsumer(String topic,String groupId){
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,KAFKA_SERVER);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);

        //FlinkKafkaConsumer kafkaConsumer = new FlinkKafkaConsumer(topic,new SimpleStringSchema(),props);
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(topic, new KafkaDeserializationSchema<String>() {
            @Override
            public boolean isEndOfStream(String nextElement) {
                return false;
            }

            @Override
            public String deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
                if(record != null && record.value() != null){
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
}
