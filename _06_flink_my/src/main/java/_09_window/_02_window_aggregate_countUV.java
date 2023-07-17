package _09_window;

import _01_tools.ClickSource;
import _01_tools.POJO_ClickEvent;
import _01_tools.POJO_UserViewCountPerWindow;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;


public class _02_window_aggregate_countUV {
    public static void main(String[] args) throws Exception {
        
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        
        executionEnvironment.addSource(new ClickSource())
          .keyBy(r -> r.name)
          
          
          .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
          .aggregate(new countAgg(),new windowResult() )
          .print();
        
        executionEnvironment.execute();
        
        
    }
    // NOTE 不需要所有的都要重写
    public static class countAgg implements AggregateFunction<POJO_ClickEvent, Long, Long> {
    
        @Override
        public Long createAccumulator() {
            return 0L;
            // NOTE
        }
    
        @Override
        public Long add(POJO_ClickEvent value, Long accumulator) {
            return accumulator+1L;
        }
    
        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }
    
        @Override
        public Long merge(Long a, Long b) {
            return null;
        }
    }
    
    
    // 继承, 不是实现
    
    public static class windowResult extends ProcessWindowFunction<Long, POJO_UserViewCountPerWindow, String, TimeWindow> {
    
        @Override
        public void process(String s, Context context, Iterable<Long> elements, Collector<POJO_UserViewCountPerWindow> out) throws Exception {
            out.collect(
              new POJO_UserViewCountPerWindow(
                s,elements.iterator().next(),
                context.window().getStart(),
                context.window().getEnd()
              )
            );
        
        }
    }
}
