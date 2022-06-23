package _14_lateness;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

public class _15_gapMerge {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new SourceFunction<String>() {
              @Override
              public void run(SourceContext<String> ctx) throws Exception {
                  ctx.collect("a");
                  Thread.sleep(1000L);
                  ctx.collect("a");
                  Thread.sleep(10 * 1000L);
                  ctx.collect("a");
                  //Thread.sleep(10 * 1000L); // 没有这句的话, 只有一个窗口. 无界流,
              }
              
              @Override
              public void cancel() {
              
              }
          })
          .keyBy(r -> true)
          .window(ProcessingTimeSessionWindows.withGap(Time.seconds(5))) // 超过gap后, 才会造个窗口出来
          .process(new ProcessWindowFunction<String, String, Boolean, TimeWindow>() {
              @Override
              public void process(Boolean key, Context ctx, Iterable<String> elements, Collector<String> out) throws Exception {
                  out.collect("key: " + key + ", 在窗口" +
                                "" + new Timestamp(ctx.window().getStart()) + "~" +
                                "" + new Timestamp(ctx.window().getEnd()) + "里面有 " +
                                "" + elements.spliterator().getExactSizeIfKnown() + " 条数据。");
              }
          })
          .print();
        
        env.execute();
    }
}
