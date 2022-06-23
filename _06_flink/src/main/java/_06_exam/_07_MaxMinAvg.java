package _06_exam;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

public class _07_MaxMinAvg {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        executionEnvironment.addSource(new SourceFunction<Integer>() {
        
            boolean isRunning = true;
            Random random = new Random();
        
            @Override
            public void run(SourceContext<Integer> ctx) throws Exception {
                while (isRunning) {
                    ctx.collect(random.nextInt(100));
                }
                Thread.sleep(1000L); // 随机产生要sleep下
            
            }
        
            @Override
            public void cancel() {
                isRunning = false;
            }
        })
          .map(new MapFunction<Integer, Tuple5<Integer, Integer, Integer, Integer, Integer>>() {
              @Override
              public Tuple5<Integer, Integer, Integer, Integer, Integer> map(Integer value) throws Exception {
                  return Tuple5.of(
                    value,
                    value,
                    value,
                    1,
                    value
              
                  );
              }
          })
          .keyBy(r -> "int")
          .reduce(new ReduceFunction<Tuple5<Integer, Integer, Integer, Integer, Integer>>() {
              @Override
              public Tuple5<Integer, Integer, Integer, Integer, Integer> reduce(
                Tuple5<Integer, Integer, Integer, Integer, Integer> in,
                Tuple5<Integer, Integer, Integer, Integer, Integer> acc) throws Exception {
                  return Tuple5.of(
                    Math.max(in.f0, acc.f0),
                    Math.min(in.f1, acc.f1),
                    in.f2 + acc.f2,
                    in.f3 + acc.f3,
                    (in.f3 + acc.f3) / (in.f2 + acc.f2)
                  );
              }
          
          })
          .print("max, min, sum, count, avg");
        
        
        executionEnvironment.execute();
    }
    
}
