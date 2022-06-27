package _18_CEP;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;

/*
使用状态机实现连续3次登录失败
 */
public class _05_FailCountbyStateMachine_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        SingleOutputStreamOperator<Event> stream =
          env
            .fromElements(
              new Event("user-1", "fail", 1000L),
              new Event("user-1", "fail", 2000L),
              new Event("user-2", "success", 3000L),
              new Event("user-1", "fail", 4000L),
              new Event("user-1", "fail", 5000L)
            )
            // flink cep 必须使用事件时间
            .assignTimestampsAndWatermarks(
              WatermarkStrategy.<Event>forMonotonousTimestamps()
                .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                    @Override
                    public long extractTimestamp(Event element, long recordTimestamp) {
                        return element.ts;
                    }
                })
            );
        
        stream
          .keyBy(r -> r.key)
          .process(new StateMachine())
          .print();
        
        env.execute();
    }
    
    public static class StateMachine extends KeyedProcessFunction<String, Event, String> {
        private HashMap<Tuple2<String, String>, String> stateMachine = new HashMap<>();
        // 保存当前的状态
        private ValueState<String> currentState;
        
        @Override
        public void open(Configuration parameters) throws Exception {
            // INITIAL状态接收到fail事件，跳转到S1状态
            stateMachine.put(Tuple2.of("INITIAL", "fail"), "S1");
            stateMachine.put(Tuple2.of("INITIAL", "success"), "SUCCESS");
            stateMachine.put(Tuple2.of("S1", "fail"), "S2");
            stateMachine.put(Tuple2.of("S1", "success"), "SUCCESS");
            stateMachine.put(Tuple2.of("S2", "fail"), "FAIL");
            stateMachine.put(Tuple2.of("S2", "success"), "SUCCESS");
            
            currentState = getRuntimeContext().getState(
              new ValueStateDescriptor<String>(
                "current-state",
                Types.STRING
              )
            );
        }
        
        @Override
        public void processElement(Event event, Context ctx, Collector<String> out) throws Exception {
            if (currentState.value() == null) {
                // 到达的是第一条数据
                currentState.update("INITIAL");
            }
            
            // 计算将要跳转到的状态
            String nextState = stateMachine.get(Tuple2.of(currentState.value(), event.value));
            if (nextState.equals("FAIL")) {
                out.collect(event.key + "连续3次登录失败。");
                // 重置到S2状态, reset通过这里实现. 也可以自己定义个reset
                currentState.update("S2");
            } else if (nextState.equals("SUCCESS")) {
                // 直接重置到INITIAL状态
                currentState.update("INITIAL");
            } else {
                // 跳转到下一个状态
                currentState.update(nextState);
            }
        }
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
