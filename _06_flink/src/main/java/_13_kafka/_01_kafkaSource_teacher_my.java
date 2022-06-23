package _13_kafka;

import com.atguigu.utils.ProductViewCountPerWindow;
import com.atguigu.utils.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
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
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;

public class _01_kafkaSource_teacher_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop102:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG , StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        
        env
          .addSource(new FlinkKafkaConsumer<String>(
            "userbehavior-0106",
            new SimpleStringSchema(),
            properties
          ))
          .flatMap(new FlatMapFunction<String, UserBehavior>() {
              @Override
              public void flatMap(String in, Collector<UserBehavior> out) throws Exception {
                  String[] array = in.split(",");
                  UserBehavior userBehavior = new UserBehavior(
                    array[0], array[1], array[2], array[3],
                    Long.parseLong(array[4]) * 1000L
                  );
                  if (userBehavior.type.equals("pv")) {
                      out.collect(userBehavior);
                  }
              }
          })
          .assignTimestampsAndWatermarks(
            // 将最大延迟时间设置为0
            WatermarkStrategy.<UserBehavior>forMonotonousTimestamps()
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
          .process(new KeyedProcessFunction<Long, ProductViewCountPerWindow, String>() {
              private ListState<ProductViewCountPerWindow> listState;
              
              @Override
              public void open(Configuration parameters) throws Exception {
                  listState = getRuntimeContext().getListState(
                    new ListStateDescriptor<ProductViewCountPerWindow>(
                      "list",
                      Types.POJO(ProductViewCountPerWindow.class)
                    )
                  );
              }
              
              @Override
              public void processElement(ProductViewCountPerWindow in, Context ctx, Collector<String> out) throws Exception {
                  listState.add(in);
                  
                  ctx.timerService().registerEventTimeTimer(
                    in.windowEndTime + 1L
                  );
              }
              
              @Override
              public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {
                  ArrayList<ProductViewCountPerWindow> arrayList = new ArrayList<>();
                  for (ProductViewCountPerWindow e : listState.get()) arrayList.add(e);
                  listState.clear();
                  
                  arrayList.sort(new Comparator<ProductViewCountPerWindow>() {
                      @Override
                      public int compare(ProductViewCountPerWindow p1, ProductViewCountPerWindow p2) {
                          return (int) (p2.count - p1.count);
                      }
                  });
                  
                  StringBuilder result = new StringBuilder();
                  result.append("=========================================\n");
                  result.append("窗口结束时间：" + new Timestamp(timerTs - 1L) + "\n");
                  for (int i = 0; i < 3; i++) {
                      ProductViewCountPerWindow tmp = arrayList.get(i);
                      result.append("第" + (i + 1) + "名的商品ID是：" + tmp.productId + "，浏览次数是：" + tmp.count + "\n");
                  }
                  result.append("=========================================\n");
                  
                  out.collect(result.toString());
              }
          })
          .print();
        
        env.execute();
    }
}
