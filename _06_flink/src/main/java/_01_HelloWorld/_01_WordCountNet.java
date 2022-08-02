package _01_HelloWorld;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _01_WordCountNet {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        DataStreamSource<String> sourceNet = executionEnvironment.socketTextStream("localhost", 9999); // nc -lk 9999 来模拟产生数据;
    
    
    
        // mappedStream
        SingleOutputStreamOperator<Tuple2<String, Integer>> mappedStream = sourceNet.flatMap(
          new FlatMapFunction<String, Tuple2<String, Integer>>() {
              @Override
              public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                  String[] s = value.split(" ");
                  for (String word : s) {
                      out.collect(Tuple2.of(word, 1));
                  }
              
              }
          }
        );
    
        KeyedStream<Tuple2<String, Integer>, String> keyedStream = mappedStream.keyBy(k -> k.f0);
        SingleOutputStreamOperator<Tuple2<String, Integer>> result = keyedStream.sum("f1");
        
        result.print();
        
        executionEnvironment.execute();
    
    }
    
}
