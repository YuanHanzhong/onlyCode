package _01_HelloWorld;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _02_WorldCountFile {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    
        // 生产环境不设置全局并行度, 因为不容易调试
        executionEnvironment.setParallelism(1);
    
        DataStreamSource<String> source = executionEnvironment.readTextFile("/Users/jack/code/only-code/_06_flink/src/main/resources/test.txt");
        SingleOutputStreamOperator<Tuple2<String, Integer>> mappedSource = source.flatMap(
          new FlatMapFunction<String, Tuple2<String, Integer>>()    {
              @Override
              public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                  String[] words = value.split(" ");
                  for (String word : words) {
                      out.collect(Tuple2.of(word, 1));
                  }
              
              }
          }
        );
    
        KeyedStream<Tuple2<String, Integer>, String> keyedStream = mappedSource.keyBy(r -> r.f0);
        SingleOutputStreamOperator<Tuple2<String, Integer>> result = keyedStream.sum("f1");
        result.print();
        executionEnvironment.execute();
    }
    
}
