package _06_exam;

import jdk.nashorn.internal.ir.WhileNode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.Random;

public class _06_MaxMinAvg_my {
    public static void main(String[] args) throws Exception {

        
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        DataStreamSource<Integer> integerDataStreamSource = executionEnvironment.addSource(new SourceFunction<Integer>() {
            Random random=new Random();
            boolean isRunning=true;
            
            @Override
            public void run(SourceContext<Integer> ctx) throws Exception {
                while (isRunning) {
    
                    ctx.collect(random.nextInt(10));
                    Thread.sleep(1000L);
                }
        
            }
    
            @Override
            public void cancel() {
                isRunning = false;
            }
        });
        
        integerDataStreamSource
          .map(new MapFunction<Integer, Tuple5<Integer,Integer,Integer,Integer,Integer>>() {
              @Override
              public Tuple5<Integer, Integer, Integer, Integer, Integer> map(Integer value) throws Exception {
                  return Tuple5.of(
                    value,
                    value,
                    value,
                    1,
                    value // 只是占个位置, 没实际意义
                  );
              }
          })
          .keyBy(new KeySelector<Tuple5<Integer, Integer, Integer, Integer, Integer>, String>() {
              @Override
              public String getKey(Tuple5<Integer, Integer, Integer, Integer, Integer> value) throws Exception {
                  return "int"; // 没有实际意义, 就是想要把所有的放在一起
              }
          })
          .reduce(new ReduceFunction<Tuple5<Integer, Integer, Integer, Integer, Integer>>() {
              @Override
              public Tuple5<Integer, Integer, Integer, Integer, Integer> reduce(Tuple5<Integer, Integer, Integer, Integer, Integer> in, Tuple5<Integer, Integer, Integer, Integer, Integer> acc) throws Exception {
                  return Tuple5.of(
                    Math.max(in.f0, acc.f0),
                    Math.min(in.f1,acc.f1),
                    in.f2+acc.f2,
                    in.f3+acc.f3, // 这里应该只有一个
                    (in.f2+acc.f2)/(in.f3+acc.f3)
                    
                  );
              }
              
          })
          .print();
    
    
        executionEnvironment.execute();
    }
}
