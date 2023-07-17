package _09_window;

import _01_tools.POJO_ClickEvent;
import _01_tools.ClickSource;
import _01_tools.POJO_UserViewCountPerWindow;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;


public class _01_window_countUV {
    public static void main(String[] args) throws Exception {
        
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        
        executionEnvironment.addSource(new ClickSource())
          .keyBy(r -> r.name)
          
          
          .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
          .process(
            new ProcessWindowFunction<POJO_ClickEvent, POJO_UserViewCountPerWindow, String, TimeWindow>() {
                @Override
                public void process(String s, Context context, Iterable<POJO_ClickEvent> elements, Collector<POJO_UserViewCountPerWindow> out) throws Exception {
                    
                    out.collect(new POJO_UserViewCountPerWindow(
                      s,
                      elements.spliterator().getExactSizeIfKnown(),
                      context.window().getStart(),
                      context.window().getEnd()
                    
                    ));
                }
            })
          .print();
        
        executionEnvironment.execute();
        
        
    }
}
