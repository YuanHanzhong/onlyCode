package _06_exam;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

// star 点 点不出来, 导包导错, 没有写在main里

public class _01_ {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        DataStreamSource<String> source = executionEnvironment.socketTextStream("hadoop102", 9999, " "); // got 决定了以什么为单位, 即发送的时机
        source.map(new MapFunction<String, String>() {
            @Override
            public String map(String value) throws Exception {
                return value; // got map里面不能有for遍历, 因为它是1进1出
            }
        })
          .print("map: ");
        
        
        // faltmap
        source.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String value, Collector<String> out) throws Exception {
                String[] words = value.split(" ");
                for (String word : words) {
                    out.collect(word);
                }

            }
        })
          .print("flatmap");

        source.filter(new FilterFunction<String>() {
            @Override
            public boolean filter(String value) throws Exception {
                if (value.length() > 2) {
                    return true;
                } else {
                    return false;
                }
            }
        })
          .print("fliter");
        
        // wordcont
        SingleOutputStreamOperator<Tuple2<String, Integer>> tuple2SingleOutputStreamOperator = source.flatMap(
          new FlatMapFunction<String, Tuple2<String, Integer>>() {
              @Override
              public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                  String[] words = value.split(" ");
                  for (String word : words) {
                      out.collect(new Tuple2(word, 1));
                  }
              
              }
          }
        );
        
        tuple2SingleOutputStreamOperator.keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
            @Override
            public String getKey(Tuple2<String, Integer> value) throws Exception {
                return value.f0;
            }
        }) // keyby 常常用匿名函数 got
        .reduce(new ReduceFunction<Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> reduce(Tuple2<String, Integer> value1, Tuple2<String, Integer> value2) throws Exception {
                return new Tuple2<>(value1.f0, value1.f1 + value2.f1);
            }
        })
          .print("wordCount: ");
        
        executionEnvironment.execute();
    }
    
}
