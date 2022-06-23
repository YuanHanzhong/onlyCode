package _09_window;

import com.atguigu.utils.ClickEvent;
import com.atguigu.utils.ClickSource;
import com.atguigu.utils.UserViewCountPerWindow;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/*
需求: 统计每个窗口的用户访问次数

 */
public class _01_Window_countUV_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        executionEnvironment
          .addSource(new ClickSource())
          .keyBy(r -> r.username)
          .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
          .process(new WindowResultMine())
          .print("");
        
        executionEnvironment.execute();
    }
    
    public static class WindowResultMine
      extends ProcessWindowFunction<ClickEvent, UserViewCountPerWindow, String, TimeWindow> {
        
        @Override
        public void process(String key, Context context, Iterable<ClickEvent> elements, Collector<UserViewCountPerWindow> out) throws Exception {
            out.collect(new UserViewCountPerWindow(
              key,
              elements.spliterator().getExactSizeIfKnown(),
              context.window().getStart(),
              context.window().getEnd()
            ));
            
        }
    }
}
