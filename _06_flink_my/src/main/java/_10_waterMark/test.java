package _10_waterMark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

// NOTE 2022/9/16
/*
收获
    把字符串转化为 Long, Long.parseLong()
    
    flatMap 使用 collect, map 直接就是 return
    
    
    分配水位线的方法:
    
        1. 调用方法 .assignTimestampsAndWaterMarks
            开箱即用的方法
                有序, formono
                乱序, foroutofOrderness, 注意指定泛型
                
        2. 在数据源中生成水位线 .collectWithTimestamp, 非常适合测试

    
 */
public class test {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        
        executionEnvironment.setParallelism(1);
        
        DataStreamSource<String> stringDataStreamSource = executionEnvironment.readTextFile("/Users/jack/code/only-code/_06_flink_my/src/main/resources/waterMarkTest.txt");
        
        stringDataStreamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Long>>() {
            @Override
            public void flatMap(String value, Collector<Tuple2<String, Long>> out) throws Exception {
                
                String[] lines = value.split(" ");
                
                out.collect(Tuple2.of(
                  lines[0],
                  Long.parseLong(lines[1]) * 1000L
                ));
                
            }
        })
          
          .assignTimestampsAndWatermarks(
            WatermarkStrategy.<Tuple2<String,Long>>forBoundedOutOfOrderness(Duration.ofSeconds(5)))
          
          
          
          
          
          
          
          
          .keyBy(r -> r.f0)
          .window(TumblingEventTimeWindows.of(Time.seconds(10L)))
          .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {
              @Override
              public void process(String s, Context context, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {
                  out.collect("key: " + s +
                                "在窗口: " + context.window().getStart() +
                                "~" + context.window().getEnd() +
                                "里的数据条数为" + elements.spliterator().getExactSizeIfKnown()
                  );
                  
              }
          })
          .print();
        executionEnvironment.execute();
        
    }
    
}
