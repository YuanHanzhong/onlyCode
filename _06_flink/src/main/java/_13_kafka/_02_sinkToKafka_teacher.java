package _13_kafka;

import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;


  
  import org.apache.flink.api.common.serialization.SimpleStringSchema;
  import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
  import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
  
  import java.util.Properties;

// sink to kafka
public class _02_sinkToKafka_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "hadoop102:9092");
        
        env
          .readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\UserBehavior.csv")
          .addSink(new FlinkKafkaProducer<String>(
            "userbehavior-0106",
            new SimpleStringSchema(),
            properties
          ));
        
        env.execute();
    }
}
