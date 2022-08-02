package _02_exe;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
/*
使用 reduce 实现 sum 的功能
 */
public class _02_WordCount_reduce {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        
        executionEnvironment.setParallelism(1);
        
        DataStreamSource<String> stringDataStreamSource = executionEnvironment.readTextFile("/Users/jack/code/only-code/_06_flink_my/src/main/resources/test.txt");
        
        stringDataStreamSource.map(new MapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String value) throws Exception {
                return Tuple2.of(value, 1);
            }
        })
          .keyBy(r -> r.f0)
          .reduce(new ReduceFunction<Tuple2<String, Integer>>() {
              @Override
              public Tuple2<String, Integer> reduce(Tuple2<String, Integer> in, Tuple2<String, Integer> out) throws Exception {
                  
                  return Tuple2.of(in.f0, in.f1 + out.f1);
              }
          })
          .print();
        
        executionEnvironment.execute();
    }
    
    
}
