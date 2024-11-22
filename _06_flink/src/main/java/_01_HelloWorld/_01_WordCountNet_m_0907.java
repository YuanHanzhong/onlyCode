package _01_HelloWorld;

// 2022/9/7 15:17 NOTE 实现从网络端口9999读取数据

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
// 2022/9/7 16:03 NOTE 不能识别是因为导错类了

public class _01_WordCountNet_m_0907 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> source = executionEnvironment.socketTextStream("localhost", 9999, "\n");

        SingleOutputStreamOperator<Tuple2<String, Integer>> tuple2SingleOutputStreamOperator = source
                .flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    // 2022/9/7 16:05 NOTE 导入tuple2时导错类了, 不能识别
                    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                        String[] s = value.split(" ");
                        for (String word : s) {
                            out.collect(Tuple2.of(word, 1));
                        }

                    }
                });

        // 2022/9/7 16:05 NOTE 选取tuple2第一个元素
        KeyedStream<Tuple2<String, Integer>, String> keyedStream = tuple2SingleOutputStreamOperator
                .keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
                    @Override
                    public String getKey(Tuple2<String, Integer> value) throws Exception {
                        return value.f0;
                    }
                });

        keyedStream
                .sum(1)
                .print();
        executionEnvironment.execute();

    }

}
