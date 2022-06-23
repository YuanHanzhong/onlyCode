package _02_Pojo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _03_CountExe {
    
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        
        executionEnvironment.setParallelism(1);
        
        DataStreamSource<String> source = executionEnvironment.readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\test.txt");
        
        SingleOutputStreamOperator<WordCountPojo> wordCountPojoSingleOutputStreamOperator =
          source.flatMap(new FlatMapFunction<String, WordCountPojo>() {
            @Override
            public void flatMap(String value, Collector<WordCountPojo> out) throws Exception {
                String[] words = value.split(" ");
                for (String word : words) {
                    out.collect(new WordCountPojo(word, 1));
                }
                
            }
        });
        
        wordCountPojoSingleOutputStreamOperator
          .setParallelism(1)
          .keyBy(r -> r.word)
          .sum("count")
          .print()
        ;
        
        executionEnvironment.execute();
        
        
    }
    
    // pojo在类得内部 got
    public static class WordCountPojo {
        
        public String word;
        public Integer count;
        
        @Override
        public String toString() {
            return "WordCountPojo{" +
                     "word='" + word + '\'' +
                     ", count=" + count +
                     '}';
        }
        
        public WordCountPojo(String word, Integer count) {
            this.word = word;
            this.count = count;
        }
        
        public WordCountPojo() {
        }
    }
}
