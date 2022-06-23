package _11_topN;


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

/*
todo topN 问题是前面所有知识点的综合, 掌握了这个即可, 值得花一天敲10遍.

 */
public class _01_HotProduct_teacher {
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
            WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(5))
              .withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                  @Override
                  public long extractTimestamp(UserBehavior element, long recordTimestamp) {
                      return element.ts;
                  }
              })
          )
          .keyBy(r -> r.productId)
          .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(5)))
          .aggregate(
            new AggregateFunction<UserBehavior, Long, Long>() {
                @Override
                public Long createAccumulator() {
                    return 0L;
                }
                
                @Override
                public Long add(UserBehavior in, Long accumulator) {
                    return accumulator + 1;
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
                public void process(String key, Context ctx, Iterable<Long> elements, Collector<ProductViewCountPerWindow> out) throws Exception {
                    out.collect(new ProductViewCountPerWindow(
                      key,
                      elements.iterator().next(),
                      ctx.window().getStart(),
                      ctx.window().getEnd()
                    ));
                }
            }
          )
          .keyBy(r -> r.windowEndTime)
          .process(new TopN(3))
          .print();
        
        env.execute();
    }
    
    public static class TopN extends KeyedProcessFunction<Long, ProductViewCountPerWindow, String> {
        private int n; // 前n名
        
        public TopN(int n) {
            this.n = n;
        }
        
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
            
            // 加1000ms是为了保证所有的ProductViewCountPerWindow全部到达
            ctx.timerService().registerEventTimeTimer(
              in.windowEndTime + 1000L
            );
        }
        
        @Override
        public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {
            // 将ListState中的数据取出并放入ArrayList
            ArrayList<ProductViewCountPerWindow> arrayList = new ArrayList<>();
            for (ProductViewCountPerWindow e : listState.get()) arrayList.add(e);
            // 由于listState中的数据已经没用了，所以清空
            listState.clear();
            
            // 按照浏览次数降序排列
            arrayList.sort(new Comparator<ProductViewCountPerWindow>() {
                @Override
                public int compare(ProductViewCountPerWindow p1, ProductViewCountPerWindow p2) {
                    return (int) (p2.count - p1.count);
                }
            });
            
            StringBuilder result = new StringBuilder();
            result.append("=========================================\n");
            result.append("窗口结束时间：" + new Timestamp(timerTs - 1000L) + "\n");
            for (int i = 0; i < n; i++) {
                ProductViewCountPerWindow tmp = arrayList.get(i);
                result.append("第" + (i + 1) + "名的商品ID是：" + tmp.productId + "，浏览次数是：" + tmp.count + "\n");
            }
            result.append("=========================================\n");
            
            out.collect(result.toString());
        }
    }
}
