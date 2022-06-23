package _09_window;

import _99_util.uv.*;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/*
使用keyedProcessWindow实现滚动窗口, ProcessWindowFunction
 */
public class _03_WindowByKeyedProcessFunc_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new ClickSource_my())
          .keyBy(r -> r.username)
          .process(new MyTumblingProcessingTimeWindow(10 * 1000L))
          .print();
        
        env.execute();
    }
    
    public static class MyTumblingProcessingTimeWindow extends KeyedProcessFunction<String, ClickEvent_my, UserViewCountPerWindow_my> {
        
        // 根据人家的, 需要输入窗口大小作为参数, 所以定义一个
        private Long windowSize;
        
        public MyTumblingProcessingTimeWindow(Long windowSize) {
            this.windowSize = windowSize;
        }
        
        
        
        // key: Tuple2<窗口开始时间，窗口结束时间>
        // value: 窗口中所有元素组成的列表
        
        // got MapState代表整个表,
        private MapState<Tuple2<Long, Long>, List<ClickEvent_my>> mapState;
        
        @Override
        public void open(Configuration parameters) throws Exception {
            mapState = getRuntimeContext().getMapState(
              new MapStateDescriptor<Tuple2<Long, Long>, List<ClickEvent_my>>(
                "windowinfo-elements",
                Types.TUPLE(Types.LONG, Types.LONG),
                Types.LIST(Types.POJO(ClickEvent_my.class))
              )
            );
        }
        
        @Override
        public void processElement(ClickEvent_my in, Context ctx, Collector<UserViewCountPerWindow_my> out) throws Exception {
            // 根据时间戳计算数据所属的窗口的开始时间
            long currTs = ctx.timerService().currentProcessingTime();
            long windowStartTime = currTs - currTs % windowSize;
            long windowEndTime = windowStartTime + windowSize;
            // 窗口信息
            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStartTime, windowEndTime);
            
            // 判断mapState中是否有windowInfo这个key
            // 也就是mapState中是否存在windowInfo这个窗口
            // 如果不存在这个窗口，说明输入数据in是属于这个窗口的第一个元素
            if (!mapState.contains(windowInfo)) {
                // 新建列表
                ArrayList<ClickEvent_my> elements = new ArrayList<>();
                // 将输入数据添加到列表中
                elements.add(in);
                // 创建一个新的窗口，窗口中只有一个元素
                mapState.put(windowInfo, elements);
            }
            // 如果windowInfo已经存在，也就是窗口已经存在
            else {
                // 直接将输入数据in添加到windowInfo对应的列表中
                mapState.get(windowInfo).add(in); // got 底层结构的关键
            }
            
            // 注册(窗口结束时间-1毫秒)的定时器
            ctx.timerService().registerProcessingTimeTimer(
              windowEndTime - 1L
            );
        }
        
        @Override
        public void onTimer(long timerTs, OnTimerContext ctx, Collector<UserViewCountPerWindow_my> out) throws Exception {
            
            // 形参都是传递信息的桥梁
            long windowEndTime = timerTs + 1L;
            long windowStartTime = windowEndTime - windowSize;
            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStartTime, windowEndTime);
            String username = ctx.getCurrentKey();
            long count = mapState.get(windowInfo).size();
            out.collect(new UserViewCountPerWindow_my(
              username,
              count,
              windowStartTime,
              windowEndTime
            ));
            // 销毁窗口, 注意不是clear
            mapState.remove(windowInfo);
        }
    }
}
