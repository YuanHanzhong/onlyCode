package _02_Pojo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _05_Exe {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> source = executionEnvironment.readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\test.txt");
        
        source
          .setParallelism(1)
          .flatMap(new Tokenizer())
          .setParallelism(1)
          .keyBy(r->r.word)
          .reduce(new Sum5())
          .print();
        
        executionEnvironment.execute();
    
    }
    
    // Sum
    public static class Sum5 implements ReduceFunction<WordCount>{
    
        @Override
        public WordCount reduce(WordCount in, WordCount out) throws Exception {
            return new WordCount(in.word, out.Count+in.Count);
        }
    }
    
    // Tokenizer
    public static class Tokenizer implements FlatMapFunction<String, WordCount>{
    
        @Override
        public void flatMap(String value, Collector<WordCount> out) throws Exception {
            String[] words = value.split(" ");
            for (String word : words) {
                out.collect(new WordCount(word,1));
            }
    
        }
    }
    
    // pojo
    public static class WordCount{
        String word;
        Integer Count;
    
        @Override
        public String toString() {
            return "WordCount{" +
                     "word='" + word + '\'' +
                     ", Count=" + Count +
                     '}';
        }
    
        public WordCount(String word, Integer count) {
            this.word = word;
            Count = count;
        }
    
        public WordCount() {
        }
    }
    
}
