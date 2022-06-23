package _08_richFunction;

import _99_util.uv.ClickEvent_my;
import _99_util.uv.ClickSource_my;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 对每个用户访问的url计数
 */
public class _09_Dic_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new ClickSource_my())
          .keyBy(r -> r.username)
          .process(new KeyedProcessFunction<String, ClickEvent_my, String>() {
              private MapState<String, Integer> urlCount;

              
              //// 写open()就是为了一些初始化的工作
              //@Override
              //public void open(Configuration parameters) throws Exception {
              //    urlCount = getRuntimeContext()
              //      .getMapState()
              //
              //
              //
              //
              //
              //}
    
              @Override
              public void processElement(ClickEvent_my value, Context ctx, Collector<String> out) throws Exception {
              
        
              }
          })
          .print();
        
        env.execute();
    }
    
    
}
