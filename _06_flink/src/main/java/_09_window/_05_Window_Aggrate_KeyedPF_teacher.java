package _09_window;

import com.atguigu.utils.ClickEvent;
import com.atguigu.utils.ClickSource;
import com.atguigu.utils.UserViewCountPerWindow;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class _05_Window_Aggrate_KeyedPF_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new ClickSource())
          .keyBy(r -> r.username)
          .process(new MyTumblingProcessingTimeWindow(10 * 1000L))
          .print();
        
        env.execute();
    }
    
    public static class MyTumblingProcessingTimeWindow extends KeyedProcessFunction<String, ClickEvent, UserViewCountPerWindow> {
        private long windowSize;
        
        public MyTumblingProcessingTimeWindow(long windowSize) {
            this.windowSize = windowSize;
        }
        
        // key: Tuple2<窗口开始时间，窗口结束时间>
        // value: 窗口的累加器
        private MapState<Tuple2<Long, Long>, Long> mapState;
        
        @Override
        public void open(Configuration parameters) throws Exception {
            mapState = getRuntimeContext().getMapState(
              new MapStateDescriptor<Tuple2<Long, Long>, Long>(
                "windowinfo-accumulator",
                Types.TUPLE(Types.LONG, Types.LONG),
                Types.LONG
              )
            );
        }
        
        @Override
        public void processElement(ClickEvent in, Context ctx, Collector<UserViewCountPerWindow> out) throws Exception {
            long currTs = ctx.timerService().currentProcessingTime();
            long windowStartTime = currTs - currTs % windowSize;
            long windowEndTime = windowStartTime + windowSize;
            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStartTime, windowEndTime);
            
            // 实现的是输入数据和累加器的聚合规则
            if (!mapState.contains(windowInfo)) {
                mapState.put(windowInfo, 1L);
            } else {
                long oldAcc = mapState.get(windowInfo);
                long newAcc = oldAcc + 1L;
                mapState.put(windowInfo, newAcc);
            }
            
            ctx.timerService().registerProcessingTimeTimer(
              windowEndTime - 1L
            );
        }
        
        @Override
        public void onTimer(long timerTs, OnTimerContext ctx, Collector<UserViewCountPerWindow> out) throws Exception {
            long windowEndTime = timerTs + 1L;
            long windowStartTime = windowEndTime - windowSize;
            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStartTime, windowEndTime);
            // 实现的是getResult
            long count = mapState.get(windowInfo);
            String username = ctx.getCurrentKey();
            out.collect(new UserViewCountPerWindow(
              username,
              count,
              windowStartTime,
              windowEndTime
            ));
            
            mapState.remove(windowInfo);
        }
    }
}
