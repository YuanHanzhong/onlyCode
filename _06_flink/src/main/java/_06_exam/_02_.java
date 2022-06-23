package _06_exam;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _02_ {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        DataStreamSource<String> source = executionEnvironment.readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\test.txt");
        
        source.map(new MapFunction<String, String>() {
            @Override
            public String map(String value) throws Exception {
                return value;
            }
        })
          .print("map: ");
    
        source.filter(new FilterFunction<String>() {
            @Override
            public boolean filter(String value) throws Exception {
                if (value.contains("jack")) {
                    return true; // ture 就是要 got , 并不是ture过滤, 不是直译
                } else {
                    return false;
                }
            }
        })
          .print("filter: ");
        
        source.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String value, Collector<String> out) throws Exception {
                String[] words = value.split(" ");
                for (String word : words) {
                    out.collect(word);
                }
    
            }
        })
          .print("flatmap: ");
        executionEnvironment.execute();
    }
    
}
