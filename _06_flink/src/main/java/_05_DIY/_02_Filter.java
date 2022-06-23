package _05_DIY;

import com.atguigu.utils.ClickEvent;
import com.atguigu.utils.ClickSource;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _02_Filter {
    public static void main(String[] args) throws Exception {
        // 环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    
        executionEnvironment.setParallelism(1);
        DataStreamSource<ClickEvent> clickEventDataStreamSource = executionEnvironment.addSource(new ClickSource());
    
    
        //匿名函数过滤
        clickEventDataStreamSource
          .filter(r -> r.username.equals("Mike"))
          .print("匿名函数过滤Mike");
        
        //匿名类过滤
        clickEventDataStreamSource
          .filter(new FilterFunction<ClickEvent>() {
              @Override
              public boolean filter(ClickEvent value) throws Exception {
                  return value.username.equals("Jack");
              }
          })
          .print("匿名类过滤Jack");
        
        //外部类过滤
        clickEventDataStreamSource
          .filter(new MyFilter())
          .print("MyFilter过滤出Tom");
        
        //flatmap实现过滤
        clickEventDataStreamSource
          .flatMap(new FlatMapFunction<ClickEvent, ClickEvent>() {
              @Override
              public void flatMap(ClickEvent value, Collector<ClickEvent> out) throws Exception {
                  if (value.username.equals("Jack")) {
                      out.collect(value);
                  }
              }
          })
          .print("用flatMap过滤Jack");
    
    
        executionEnvironment.execute();
    }
    
    public static class MyFilter implements FilterFunction<ClickEvent> {
    
        @Override
        public boolean filter(ClickEvent value) throws Exception {
            return value.username.equals("Tom");
        }
    }
    
}
