package _02_Pojo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _06_Exe {
    
    // 输出格式和main有关
    
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> source = executionEnvironment.readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\test.txt");
        
        source
          .setParallelism(1)
          .flatMap(new _0_.Tokenizer())
          .setParallelism(1)
          .keyBy(r -> r.word)
          .reduce(new _0_.Sum())
          .setParallelism(1)
          .print()
          .setParallelism(1); // 输出格式是  任务槽>toString
        executionEnvironment.execute();
    
    }
    
    public static class Sum implements ReduceFunction<_0_.WordCount> {
        @Override
        public _0_.WordCount reduce(_0_.WordCount in, _0_.WordCount acc) throws Exception {
            return new _0_.WordCount(
              in.word,
              in.count + acc.count
            );
        }
    }
    
    public static class Tokenizer implements FlatMapFunction<String, _0_.WordCount> {
        @Override
        public void flatMap(String in, Collector<_0_.WordCount> out) throws Exception {
            String[] words = in.split(" ");
            for (String word : words) {
                out.collect(new _0_.WordCount(
                  word,
                  1
                ));
            }
        }
    }
    
    // POJO Class, 借助反射, 序列化, 需要pojo got
    // 用来模拟scala的样例类：case class WordCount(word: String, count: Int)
    // 1. 必须是公有类
    // 2. 所有字段必须是公有字段
    // 3. 必须有空构造器
    public static class WordCount {
        public String word;
        public Integer count;
        
        public WordCount() {
        }
        
        public WordCount(String word, Integer count) {
            this.word = word;
            this.count = count;
        }
        
        @Override
        public String toString() {
            return "WordCount{" +
                     "word='" + word + '\'' +
                     ", count=" + count +
                     '}';
        }
    }
}
