package _01_HelloWorld;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _02_WorldCountFile_m_09073 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
    
        DataStreamSource<String> source = executionEnvironment.readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\java\\_01_HelloWorld\\_02_WorldCountFile_m_09072.java");
    
        source.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                String[] words = value.split(" ");
                for (String word : words) {
                
                    out.collect(Tuple2.of(word, 1));
                }
            
            }
        })
          .keyBy(r -> r.f0)
          .sum(1)
        .print().setParallelism(2)
        ;
        // 2022/9/7 18:51 NOTE 写上job name很重要, 便于排错
        executionEnvironment.execute("countFile");
    }
}
