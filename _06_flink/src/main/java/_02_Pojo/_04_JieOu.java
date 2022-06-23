package _02_Pojo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _04_JieOu {
    public static void main(String[] args) throws Exception {
    
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(3);
        DataStreamSource<String> source = executionEnvironment.fromElements(" 1 1 1 2 1 2");
    
    
        
        // Tokenizer 分词器
        source
          .flatMap(new Tokenizer())
          .setParallelism(2)
          .keyBy(r -> r.word)
          .reduce(new Sum())
          .setParallelism(1)
          .print()
          .setParallelism(1);
    
        executionEnvironment.execute();
    }
    
    // Sum
    public static class Sum implements ReduceFunction<WordCount>{
    
    
        @Override
        public WordCount reduce(WordCount in, WordCount acc) throws Exception {
            return new WordCount(in.count+acc.count, in.word);
        }
    }
    
    // implement Flatmap
    public static class Tokenizer implements FlatMapFunction<String, WordCount>{
        @Override
        public void flatMap(String value, Collector<WordCount> out) throws Exception {
            String[] words = value.split(" ");
            for (String word : words) {
                out.collect(new WordCount(1,word));
            }
    
        }
    }
    
    
    
    // pojo
    public static class WordCount{
        public  Integer count;
        public  String word; // 顺寻还是比较重要的, 因为是自动不全的, 直接影响值观感受 got
    
        @Override
        public String toString() {
            return "WordCount{" +
                     "count=" + count +
                     ", word='" + word + '\'' +
                     '}';
        }
    
        public WordCount() {
        }
    
        public WordCount(Integer count, String word) {
            this.count = count;
            this.word = word;
        }
    }
    
}
