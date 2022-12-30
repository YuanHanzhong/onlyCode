package _01_HelloWorld;


import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _01_WordCountNet_m_09072 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
    
        DataStreamSource<String> source = executionEnvironment.socketTextStream("hadoop102", 9999);
        
        source.flatMap(new FlatMapFunction<String, Tuple2<String,Integer>>() {
            @Override
            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                String[] words = value.split(" ");
                for (String word : words) {
                    out.collect(Tuple2.of(word,1));
                }
    
            }
        })
          .keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
              @Override
              public String getKey(Tuple2<String, Integer> value) throws Exception {
                  
                  return value.f0;
              }
          })
          .sum(1)
          .print();
    
        executionEnvironment.execute("this is job name");
    }
}
