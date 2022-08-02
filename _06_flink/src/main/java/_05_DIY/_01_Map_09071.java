package _05_DIY;

import com.atguigu.utils.ClickEvent;
import com.atguigu.utils.ClickSource;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _01_Map_09071 {
    public static void main(String[] args) throws Exception {
        // 环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);

        DataStreamSource<ClickEvent> clickEventDataStreamSource = executionEnvironment
                                                                    .addSource(new ClickSource());
        DataStreamSource<ClickEvent> clickEventDataStreamSource1 = executionEnvironment.addSource(new ClickSource());
    
        // map 匿名函数, 取出某字段
    
        clickEventDataStreamSource.map(r -> r.username)
          .print("匿名函数");
    
        //匿名类, 取出某字段
        clickEventDataStreamSource
          .map(new MapFunction<ClickEvent, String>() {
            @Override
            public String map(ClickEvent value) throws Exception {
                return value.username;
            }
        })
          .print("匿名类");
          
        //外部类实现去除某字段
        clickEventDataStreamSource
          .map(new MyMap())
          .print("外部类");
    
        //flatmap实现取出姓名
        clickEventDataStreamSource1
          .flatMap(new FlatMapFunction<ClickEvent, String>() {
              @Override
              public void flatMap(ClickEvent value, Collector<String> out) throws Exception {
                  out.collect(value.username);
              }
          })
          .print("flatmap实现取出姓名");
        
        
    
    
    
        executionEnvironment.execute();
        
    }
    
    public static class MyMap implements MapFunction<ClickEvent, String>{
    
        @Override
        public String map(ClickEvent value) throws Exception {
            return value.username;
        }
    }
    
}
