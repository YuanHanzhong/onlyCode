package _10_waterMark;

import com.atguigu.utils.ProductViewCountPerWindow;
import com.atguigu.utils.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

// 每件商品在每个窗口的浏览次数
// 每个窗口中浏览次数最多的商品
public class _04_AliHotProduct_mine_manual {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        
        executionEnvironment
          .readTextFile("D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\UserBehavior.csv")
          // 把原始数据转换成我擅长处理的格式
          .map(new MapFunction<String, UserBehavior>() {
              @Override
              public UserBehavior map(String value) throws Exception {
                  String[] words = value.split(",");
                  return new UserBehavior( // 正好对应进去了, 不用for循环
                    words[0],
                    words[1],
                    words[2],
                    words[3],
                    Long.parseLong(words[4]) * 1000L
                  );
              }
          })
          // 处理数据前先过滤数据
          .filter(r -> r.type.equals("pv"))
          // ask +1000L保证都到达,
           //过滤了就插入水位线, 注意指定泛型
          .assignTimestampsAndWatermarks(
// todo 插入水位线再练习, 各种情况的插入
//            WatermarkStrategy.<_10_UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(0))
            //.withTimestampAssigner(new SerializableTimestampAssigner<_10_UserBehavior>())
  
            WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(0))
              .withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                  @Override
                  public long extractTimestamp(UserBehavior element, long recordTimestamp) {
                      return element.ts;
                  }
              })
          )
          
          // 统计计算前先keyBy, 统计谁就keyby谁
          .keyBy(r -> r.productId)
          
          // 开窗口, 分而治之, 自身带有时间戳的, 开SlidingEventTimeWindow. 有了水位线后, 常常带有Event字样
          .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(5))) // 参数提示的时候, 不是new, 就是 .
          
          // 有了窗口就process(aggregate), 聚合计算
          //    若用累加器的思想, 就用aggregate.
          //    若积累所有的元素, 就用process
          .aggregate(new MyCountAggrate(), new MyWindowResult())
          
         // todo 再分一次组, 目的是什么. 以上是每一个商品在一个窗口的浏览次数
          // 接下来再进行keyBy, 以统计top
          .print()
        ;
        
        executionEnvironment.execute();
    }
    
    public static class MyWindowResult extends ProcessWindowFunction<Long, ProductViewCountPerWindow, String, TimeWindow> {
    
        @Override
        public void process(String key, Context context, Iterable<Long> elements, Collector<ProductViewCountPerWindow> out) throws Exception {
            out.collect(new ProductViewCountPerWindow(
              key,
              elements.iterator().next(), // 迭代器里只有1个key, 因为已经聚合过了
              context.window().getStart(),
              context.window().getEnd()
            ));
        
        }
    }
    
    
    // ---> window ---> aggregate , 所以aggregate的泛型, 需要根据window的输出. 要对的上
    public static class MyCountAggrate implements AggregateFunction<UserBehavior,Long, Long> {
    
        @Override
        public Long createAccumulator() {
            return 0L;
        }
    
        @Override
        public Long add(UserBehavior value, Long accumulator) {
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
    

}
