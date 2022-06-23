package _06_exam;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _09_ClickCount1 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        DataStreamSource<exeClickEvent> clickEventDataStreamSource = executionEnvironment.addSource(new exeClickSource());
    
        clickEventDataStreamSource.flatMap(new FlatMapFunction<exeClickEvent, Tuple4<String, String, Long, Long>>() {
            @Override
            public void flatMap(exeClickEvent value, Collector<Tuple4<String, String, Long, Long>> out) throws Exception {
                out.collect(Tuple4.of(
                  value.username,
                  value.url,
                  value.ts,
                  1L
                ));
            
            }
        })
          .keyBy(r -> "all")
          .reduce(new ReduceFunction<Tuple4<String, String, Long, Long>>() {
              @Override
              public Tuple4<String, String, Long, Long> reduce(
                Tuple4<String, String, Long, Long> value1,
                Tuple4<String, String, Long, Long> value2) throws Exception {
                  return Tuple4.of(
                    value1.f0,
                    value1.f1,
                    value1.f2,
                    value1.f3 + value2.f3
                  );
              }
          })
          .print("name, url, ts, count: ");
    
        executionEnvironment.execute();
    }
}
