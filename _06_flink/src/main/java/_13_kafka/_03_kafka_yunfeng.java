package _13_kafka;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class _03_kafka_yunfeng {
    public static void main(String[] args)   {
        
        //获取环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        env.setParallelism(2);
        
        //kafka source配置
        
        KafkaSource<String> ks = KafkaSource.<String>builder()
                                   .setBootstrapServers("hadoop102:9092")
                                   .setTopics("yunfeng")
                                   .setGroupId("flink_kafkaSource")
                                   .setStartingOffsets(OffsetsInitializer.latest())
                                   .setValueOnlyDeserializer(new SimpleStringSchema())
                                   .build();
        
        
        DataStreamSource<String> ksdstream = env.fromSource(ks, WatermarkStrategy.noWatermarks(), "zhengguiqiang======");
        ksdstream.print();
        
        
        System.out.println("结束了");
        try {
            env.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    
    
    }
}
