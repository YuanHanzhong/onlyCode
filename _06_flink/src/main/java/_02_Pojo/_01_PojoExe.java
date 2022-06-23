package _02_Pojo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _01_PojoExe {
    public static class WordCountPojo {
        Integer count;
        String word;
        
        public WordCountPojo(Integer count, String word) { // got 参数的顺序, 最好符合习惯, 要不然容易错
            this.count = count;
            this.word = word;
        }
        
        public WordCountPojo() {
        }
        
        @Override
        public String toString() {
            return "WordCountPojo{" +
                     "count=" + count +
                     ", word='" + word + '\'' +
                     '}';
        }
    }
    
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        DataStreamSource<String> source = executionEnvironment.fromElements("hello jack hello flink hello teacher");
    
    
        SingleOutputStreamOperator<_03_CountExe.WordCountPojo> wordCountPojoSingleOutputStreamOperator = source.flatMap(new FlatMapFunction<String, _03_CountExe.WordCountPojo>() {
            @Override
            public void flatMap(String value, Collector<_03_CountExe.WordCountPojo> out) throws Exception {
                String[] words = value.split(" ");
                for (String word : words) {
    
                    out.collect(new _03_CountExe.WordCountPojo(word, 1));
                }
            
            }
        });
    
        wordCountPojoSingleOutputStreamOperator
          .keyBy(r -> r.word)
          .sum("count")
          .print();
        
        
        executionEnvironment.execute();
    }
}
