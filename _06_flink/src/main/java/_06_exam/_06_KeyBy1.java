package _06_exam;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class _06_KeyBy1 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    
        executionEnvironment.setParallelism(1);
        executionEnvironment
          .fromElements(1, 2, 3, 4, 5, 6, 7, 8)
          .keyBy(new KeySelector<Integer, Integer>() {
              @Override
              public Integer getKey(Integer value) throws Exception {
                  return value % 3;
              }
          })
          .reduce(new ReduceFunction<Integer>() {
              @Override
              public Integer reduce(Integer in, Integer acc) throws Exception {
                  return in+acc;
                  
              }
          })
          .setParallelism(4)
          .print("keyby")
          .setParallelism(4) // 会有4个并行的进程出来, 不设置则为默认的1个
        
        
        ;
        
        executionEnvironment.execute();
    }
    
}
