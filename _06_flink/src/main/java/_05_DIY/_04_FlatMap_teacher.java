package _05_DIY;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _04_FlatMap_teacher {
    /**
     * FlatMap对颜色处理, gray 过滤, red 复制2份, black 反转, 其他不管
     */

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
    
        DataStreamSource<String> source = executionEnvironment.fromElements("red", "black", "gray","hello");
    
        source
          .flatMap(new FlatMapFunction<String, String>() {
              @Override
              public void flatMap(String value, Collector<String> out) throws Exception {
                  if (value.equals("gray")) {
                  
                  } else if (value.equals("red")) {
                      out.collect(value);
                      out.collect(value);
                  
                  } else if (value.equals("black")) {
                      out.collect("kcalb");
                  }
              
              }
          })
          .print("匿名内部类");
    
        source
          .flatMap(
            (String value, Collector<String> out) -> {
                if (value.equals("gray")) {
                
                } else if (value.equals("red")) {
                    out.collect(value);
                    out.collect(value);
                
                } else if (value.equals("black")) {
                    out.collect("kcalb");
                }
            }
      
          )
          .returns(Types.STRING)
          .print("匿名函数");
    
    
        executionEnvironment.execute();
    }
}
