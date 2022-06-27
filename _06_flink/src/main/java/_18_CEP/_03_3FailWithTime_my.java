package _18_CEP;

import com.google.common.eventbus.EventBus;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternFlatTimeoutFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.Map;

// 超时订单检测
public class _03_3FailWithTime_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        DataStreamSource<Event> stream =
          env
            .addSource(new SourceFunction<Event>() {
                @Override
                public void run(SourceContext<Event> ctx) throws Exception {
                    ctx.collectWithTimestamp(new Event("order-1", "create", 1000L), 1000L);
                    ctx.collectWithTimestamp(new Event("order-2", "create", 2000L), 2000L);
                    ctx.collectWithTimestamp(new Event("order-1", "pay", 3000L), 3000L);
                }
                
                @Override
                public void cancel() {
                
                }
            });
        
        // 定义模板
        Pattern<Event, Event> pattern =
          Pattern
            .<Event>begin("create-order")
            .where(new SimpleCondition<Event>() {
                @Override
                public boolean filter(Event event) throws Exception {
                    return event.value.equals("create");
                }
            })
            .next("pay-order")
            .where(new SimpleCondition<Event>() {
                @Override
                public boolean filter(Event event) throws Exception {
                    return event.value.equals("pay");
                }
            })
            // 模板中的两个事件在5秒钟之内发生
            .within(Time.seconds(5));
        
        SingleOutputStreamOperator<String> result =
          CEP
            .pattern(stream.keyBy(r -> r.key), pattern)
            .flatSelect(
              // 定义侧输出流
              new OutputTag<String>("timeout-order") {
              },
              new PatternFlatTimeoutFunction<Event, String>() {
                  @Override
                  public void timeout(Map<String, List<Event>> map, long timeoutTimestamp, Collector<String> out) throws Exception {
    
                      Event create = map.get("create-order").get(0);
                      out.collect(create.key + "超时未支付");
                  }
              },
              
              // 把正常支付的挑出来
              new PatternFlatSelectFunction<Event, String>() {
                  @Override
                  public void flatSelect(Map<String, List<Event>> pattern, Collector<String> out) throws Exception {
                      Event create = pattern.get("create-order").get(0);
                      Event pay = pattern.get("pay-order").get(0);
                      
                      out.collect(create.key+" "+ pay.ts);
    
                  }
              }
            );
        
        result.print("主流");
        
        result.getSideOutput(new OutputTag<String>("timeout-order") {
        }).print("侧输出流");
        
        env.execute();
    }
    
    public static class Event {
        public String key;
        public String value;
        public Long ts;
        
        public Event() {
        }
        
        public Event(String key, String value, Long ts) {
            this.key = key;
            this.value = value;
            this.ts = ts;
        }
        
        @Override
        public String toString() {
            return "(" +
                     key +
                     "," + value +
                     "," + ts +
                     ")";
        }
    }
}
