package _05_DIY;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class _06_KeyBy {
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
          .setParallelism(4)  // got set的对象是离它最近的那个
          .print("keyby") // got 多个插槽时, 多个 Para时, > 前面的是插槽ID, ID从1计数
          .setParallelism(4) // ask 设置print的并行度为4后, 似乎对数据结果没有影响, 怎么解释? 输出的顺序时由谁决定的?
        ;
        
        executionEnvironment.execute();
    }
    
}
