package _02_exe;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/*
// NOTE 2022/9/14
需求: hello word
备注:
    02:46
    03:05
    02:13
    02:34
学到的知识:
    Tuple2 的使用
        导入包时, 注意导 flink 的, 而不是 scala 的
        1 个 p
        指定泛型
        
    到哪里了, 变量命名, 见名知意
    
    map 阶段, 收集数据时,
        用迭代的方式, 收集个数不定的数据
        有固定格式时, 就不用迭代了, 还是看想要什么数据, 有什么数据
        
    keyby 之后 reduce,
        reduce阶段可以定义比 sum 复杂的多的计算逻辑, reduce可以实现 sum 的功能
*/
public class _01_WordCount {
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
              public Tuple2<String, Integer> reduce(Tuple2<String, Integer> value1, Tuple2<String, Integer> value2) throws Exception {
                  return Tuple2.of(value1.f0,value1.f1+value2.f1);
              }
          })
          .print();
        executionEnvironment.execute();
    }
}
