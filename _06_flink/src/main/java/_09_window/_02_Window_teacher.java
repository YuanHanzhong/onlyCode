package _09_window;


import com.atguigu.utils.ClickEvent;
import com.atguigu.utils.ClickSource;
import com.atguigu.utils.UserViewCountPerWindow;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class _02_Window_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new ClickSource())
          .keyBy(r -> r.username)
          .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
          // 当AggregateFunction和ProcessWindowFunction结合使用时，
          // 需要调用aggregate方法
          .aggregate(
            new CountAgg(),
            new WindowResult()
          )
          .print();
        
        env.execute();
    }
    
    public static class CountAgg implements AggregateFunction<ClickEvent, Long, Long> {
        // 创建窗口时，初始化一个累加器
        @Override
        public Long createAccumulator() {
            return 0L;
        }
        
        // 每来一条数据，累加器加一
        @Override
        public Long add(ClickEvent in, Long accumulator) {
            return accumulator + 1L;
        }
        
        // 窗口闭合时，触发调用
        // 将返回值发送出去
        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }
        
        @Override
        public Long merge(Long a, Long b) {
            return null;
        }
    }
    
    // 输入的泛型是Long，也就是AggregateFunction输出的泛型
    public static class WindowResult extends ProcessWindowFunction<Long, UserViewCountPerWindow, String, TimeWindow> {
        @Override
        public void process(String key, Context ctx, Iterable<Long> elements, Collector<UserViewCountPerWindow> out) throws Exception {
            // Iterable<Long> elements中只有一个元素
            // 也就是getResult的返回值
            out.collect(new UserViewCountPerWindow(
              key,
              // 将迭代器中唯一的元素取出
              elements.iterator().next(),
              ctx.window().getStart(),
              ctx.window().getEnd()
            ));
        }
    }
}
