package _10_waterMark;

import com.atguigu.utils.ProductViewCountPerWindow;
import com.atguigu.utils.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
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
import java.util.ArrayList;
import java.util.Comparator;

// 需求: 每件商品在每个窗口的浏览次数
// 每个窗口中浏览次数最多的商品
public class _04_AliHotProduct_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\UserBehavior.csv")
          .map(new MapFunction<String, UserBehavior>() {
              @Override
              public UserBehavior map(String in) throws Exception {
                  String[] array = in.split(",");
                  return new UserBehavior(
                    array[0], array[1], array[2], array[3],
                    Long.parseLong(array[4]) * 1000L
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
              private ListState<ProductViewCountPerWindow> listState;
              
              @Override
              public void open(Configuration parameters) throws Exception {
                  listState = getRuntimeContext().getListState(
                    new ListStateDescriptor<ProductViewCountPerWindow>(
                      "list-state",
                      Types.POJO(ProductViewCountPerWindow.class)
                    )
                  );
              }
              
              @Override
              public void processElement(ProductViewCountPerWindow in, Context ctx, Collector<String> out) throws Exception {
                  listState.add(in);
                  
                  ArrayList<ProductViewCountPerWindow> arrayList = new ArrayList<>();
                  for (ProductViewCountPerWindow e : listState.get()) arrayList.add(e);
                  
                  arrayList.sort(new Comparator<ProductViewCountPerWindow>() {
                      @Override
                      public int compare(ProductViewCountPerWindow p1, ProductViewCountPerWindow p2) {
                          return (int) (p2.count - p1.count);
                      }
                  });
                  
                  StringBuilder result = new StringBuilder();
                  result.append("=======================================\n");
                  ProductViewCountPerWindow topProduct = arrayList.get(0);
                  result.append("窗口：" + new Timestamp(topProduct.windowStartTime) + "~" +
                                  "" + new Timestamp(topProduct.windowEndTime) + "\n");
                  result.append("第一名：" + topProduct + "\n");
                  result.append("=======================================\n");
                  
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
