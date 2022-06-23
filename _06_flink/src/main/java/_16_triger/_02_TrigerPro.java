package _16_triger;

import com.atguigu.utils.ClickEvent;
import com.atguigu.utils.ClickSource;
import com.atguigu.utils.ProductViewCountPerWindow;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
/*
需求: 滚动窗口是1天, 但是想要每1秒看一眼

 */
public class _02_TrigerPro {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new ClickSource())
          .assignTimestampsAndWatermarks(
            WatermarkStrategy.<ClickEvent>forMonotonousTimestamps()
              .withTimestampAssigner(new SerializableTimestampAssigner<ClickEvent>() {
                  @Override
                  public long extractTimestamp(ClickEvent element, long recordTimestamp) {
                      return element.ts;
                  }
              })
          )
          .keyBy(r -> r.username)
          .window(TumblingEventTimeWindows.of(Time.days(1)))
          .trigger(new Trigger<ClickEvent, TimeWindow>() {
              @Override
              public TriggerResult onElement(ClickEvent in, long l, TimeWindow window, TriggerContext ctx) throws Exception {
                  // 窗口的第一条数据的时间戳接下来的所有整数秒都触发窗口计算
                  
                  // 下面的状态变量是每个窗口独有的
                  ValueState<Boolean> flag = ctx.getPartitionedState(
                    new ValueStateDescriptor<Boolean>(
                      "flag",
                      Types.BOOLEAN
                    )
                  );
                  
                  // 说明到达的数据是窗口的第一条数据
                  if (flag.value() == null) {
                      // 计算第一条数据接下来的整数秒
                      // 1234ms + 1000ms - 1234 % 1000 -> 2000ms
                      long nextSecond = in.ts + 1000L - in.ts % 1000L;
                      // 注册事件时间定时器onEventTime方法
                      ctx.registerEventTimeTimer(nextSecond);
                      
                      // 将标志位置为true
                      flag.update(true);
                  }
                  
                  return TriggerResult.CONTINUE;
              }
              
              @Override
              public TriggerResult onProcessingTime(long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                  return null;
              }
              
              @Override
              public TriggerResult onEventTime(long timerTs, TimeWindow window, TriggerContext ctx) throws Exception {
                  if (timerTs < window.getEnd()) {
                      if (timerTs + 1000L < window.getEnd()) {
                          // 这里注册的定时器还是onEventTime方法
                          ctx.registerEventTimeTimer(timerTs + 1000L);
                      }
                      // 触发窗口计算
                      return TriggerResult.FIRE;
                  }
                  return TriggerResult.CONTINUE;
              }
              
              @Override
              public void clear(TimeWindow timeWindow, TriggerContext ctx) throws Exception {
                  ValueState<Boolean> flag = ctx.getPartitionedState(
                    new ValueStateDescriptor<Boolean>(
                      "flag",
                      Types.BOOLEAN
                    )
                  );
                  // 窗口闭合时，清空窗口状态变量flag
                  flag.clear();
              }
          })
          .aggregate(
            new AggregateFunction<ClickEvent, Long, Long>() {
                @Override
                public Long createAccumulator() {
                    return 0L;
                }
                
                @Override
                public Long add(ClickEvent value, Long accumulator) {
                    return accumulator + 1L;
                }
                
                @Override
                public Long getResult(Long accumulator) {
                    return accumulator;
                }
                
                @Override
                public Long merge(Long a, Long b) {
                    return null;
                }
            },
            new ProcessWindowFunction<Long, ProductViewCountPerWindow, String, TimeWindow>() {
                @Override
                public void process(String key, Context ctx, Iterable<Long> iterable, Collector<ProductViewCountPerWindow> out) throws Exception {
                    out.collect(new ProductViewCountPerWindow(
                      key,
                      iterable.iterator().next(),
                      ctx.window().getStart(),
                      ctx.window().getEnd()
                    ));
                }
            }
          )
          .print();
        
        env.execute();
    }
}
