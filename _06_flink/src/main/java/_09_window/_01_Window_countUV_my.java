package _09_window;

import _99_util.uv.ClickEvent_my;
import _99_util.uv.ClickSource_my;
import _99_util.uv.UserViewCountPerWindow_my;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/*
需求: 统计每个窗口的用户访问次数
思路:
    因为自己不带时间戳, 所以开一个滚动处理时间窗口, 窗口大小定为10
    开窗之后2个选择
        1. 使用process, 会收集所有的元素, process选用1个参数的就足够了
        2. 使用aggregate, 用了累加器的思想
GOT
    1. 参数提示不是什么时候都准
        1.1 缺的要么new, 要么 .
        1.2 参数有可能提示多了, 有可能提示少了, 多试试, 达到目的即可, 不需要全都懂
        1.3 个别的参数需要多写, 用时会查, 不用故意记(比忘)
    2. 输入输出类型都可以为POJO
 */
public class _01_Window_countUV_my {
    public static void main(String[] args) throws Exception {
        
        // 设计环境变量2+1
        
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        // 具体设置, 添加源,
        executionEnvironment
          .addSource(new ClickSource_my())
          .keyBy(r -> r.username)
          .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
          .process(new WindowResultMine())
          .print();
        executionEnvironment.execute();
    }
    
    public static class WindowResultMine
      extends ProcessWindowFunction<ClickEvent_my, UserViewCountPerWindow_my, String, TimeWindow> {
        
        
        @Override
        public void process(String s, Context context, Iterable<ClickEvent_my> elements, Collector<UserViewCountPerWindow_my> out) throws Exception {
            out.collect(new UserViewCountPerWindow_my(
              s,
              elements.spliterator().getExactSizeIfKnown(),
              context.window().getStart(),
              context.window().getEnd()
            
            ));
        }
    }
}
