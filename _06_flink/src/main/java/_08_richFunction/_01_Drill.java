package _08_richFunction;

import org.apache.flink.api.common.functions.AbstractRichFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _01_Drill {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment
          .setParallelism(1)
          .fromElements(1,2,3,4,5,6,7,8)
          .flatMap(new RichFlatMapFunction<Integer, String>() { // GOT 只需要记住, Rich就是多了open和close方法, 并且多了很多函数来调取信息
              @Override
              public void open(Configuration parameters) throws Exception {
                  System.out.println("open: " + getRuntimeContext().getIndexOfThisSubtask());
              }
    
              @Override
              public void flatMap(Integer value, Collector<String> out) throws Exception {
                  out.collect("flatmap正在处理: \n"+value+
                               "索引"+getRuntimeContext().getIndexOfThisSubtask());
        
              }
    
              @Override
              public void close() throws Exception {
                  System.out.println("close"+getRuntimeContext().getIndexOfThisSubtask());
              }
          })
          .setParallelism(4)
          .print()
          .setParallelism(4);
        
        
        executionEnvironment.execute();
    }
    
}
