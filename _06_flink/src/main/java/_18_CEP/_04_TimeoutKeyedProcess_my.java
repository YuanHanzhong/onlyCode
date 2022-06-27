package _18_CEP;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

/*
使用keyedProcessFunction实现超时检测, 这里只是为了加深理解, 生产环境用CEP就好
 */
public class _04_TimeoutKeyedProcess_my {
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
        
        stream
          .keyBy(r -> r.key)
          .process(new KeyedProcessFunction<String, Event, String>() {
              private ValueState<Event> state;
              
              @Override
              public void open(Configuration parameters) throws Exception {
                  state = getRuntimeContext().getState(
                    new ValueStateDescriptor<Event>(
                      "state",
                      Types.POJO(Event.class)
                    )
                  );
              }
              
              @Override
              public void processElement(Event event, Context ctx, Collector<String> out) throws Exception {
                  if (event.value.equals("create")) {
                      state.update(event);
                      ctx.timerService().registerEventTimeTimer(
                        event.ts + 5000L
                      );
                  } else if (event.value.equals("pay")) {
                      out.collect(event.key + "正常支付。");
                      state.clear(); // 删除保存的create事件
                  }
              }
              
              @Override
              public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                  if (state.value() != null && state.value().value.equals("create")) {
                      out.collect(state.value().key + "超时未支付。");
                  }
              }
          })
          .print();
        
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
