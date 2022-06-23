package _06_exam;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _03_WordCount {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
    
        executionEnvironment
          .addSource(new exeClickSource())
          .flatMap(new FlatMapFunction<exeClickEvent, Tuple3<String,Integer,Long>>() {
              @Override
              public void flatMap(exeClickEvent value, Collector<Tuple3<String, Integer, Long>> out) throws Exception {
                  if (value.username.equals("mike")) {
        
                  } else if (value.username.equals("jack")){
                      out.collect(Tuple3.of(value.username,1,value.ts));
                      out.collect(Tuple3.of(value.username,0,value.ts));
                  } else {
                      out.collect(Tuple3.of(value.username,1,value.ts));
    
                  }
        
              }
          })
          .keyBy(r -> r.f0)
          .sum("f1")
          .print("myClickCount");
        
        executionEnvironment.execute();
    }
    
}
