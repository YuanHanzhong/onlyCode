package _10_waterMark;

import com.atguigu.utils.ProductViewCountPerWindow;
import com.atguigu.utils.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.time.Duration;

// 每件商品在每个窗口的浏览次数
// 每个窗口中浏览次数最多的商品
public class _04_AliHotProduct_mine {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\UserBehavior.csv")
          .map(new MapFunction<String, UserBehavior>() {
              @Override
              public UserBehavior map(String in) throws Exception {
                  String[] array = in.split(","); // 看的是每行中是用什么分割的
                  return new UserBehavior(
                    array[0], array[1], array[2], array[3],
                    Long.parseLong(array[4]) * 1000L // 把字符串解析为long
                  );
              }
          })
          .filter(r -> r.type.equals("pv"))
          .assignTimestampsAndWatermarks(
            WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(0))
              .withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                  @Override
                  public long extractTimestamp(UserBehavior element, long recordTimestamp) {
                      return element.ts;
                  }
              })
          )
          .keyBy(r -> r.productId)
          .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(5)))
          .aggregate(new CountAgg(), new WindowResult())
          // 按照窗口信息对(每个商品在每个窗口的浏览次数)这条流进行分组
          .keyBy(r -> r.windowEndTime)
          .process(new KeyedProcessFunction<Long, ProductViewCountPerWindow, String>() {
              private ValueState<Long> maxValueState;
              
              
              @Override
              public void open(Configuration parameters) throws Exception {
                  maxValueState = getRuntimeContext().getState(new ValueStateDescriptor<Long>(
                    "max-valueState",
                    Types.LONG
                  ));
              }
              
              @Override
              public void processElement(ProductViewCountPerWindow in, Context ctx, Collector<String> out) throws Exception {
    
                  if (maxValueState.value() != null) {
                      if (maxValueState.value() < in.count) {
                          maxValueState.update(in.count);
            
                      }
                  } else {
                      maxValueState.update(in.count);
                  }
                
    
                  StringBuilder result = new StringBuilder();
                  result.append("=======================================\n");
                  result.append("窗口：" + new Timestamp(in.windowStartTime) + "~" +
                                  "" + new Timestamp(in.windowEndTime) + "\n");
                  result.append("第一名：" + maxValueState.value() + "\n");
                  result.append("=======================================\n");
    
                  // 处理完一个窗口要把最大值置零
                  out.collect(result.toString());
              }
          })
          .print();
        
        env.execute();
    }
    
    public static class WindowResult extends ProcessWindowFunction<Long, ProductViewCountPerWindow, String, TimeWindow> {
        @Override
        public void process(String key, Context ctx, Iterable<Long> elements, Collector<ProductViewCountPerWindow> out) throws Exception {
            out.collect(new ProductViewCountPerWindow(
              key,
              elements.iterator().next(),
              ctx.window().getStart(),
              ctx.window().getEnd()
            ));
        }
    }
    
    public static class CountAgg implements AggregateFunction<UserBehavior, Long, Long> {
        @Override
        public Long createAccumulator() {
            return 0L;
        }
        
        @Override
        public Long add(UserBehavior in, Long accumulator) {
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
    }
}
