package _05_DIY;

import _99_util.uv.ClickEvent_my;
import _99_util.uv.ClickSource_my;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _03_FlatMap_My {
    
    
    public static void main(String[] args) throws Exception {
        
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
        executionEnvironment
          .addSource(new ClickSource_my())
          .flatMap(
            (ClickEvent_my value, Collector<ClickEvent_my> out) -> {
                out.collect(value);
            }
          )
          //.returns(Types.POJO(ClickEvent.class))
          .returns(Types.POJO(ClickEvent_my.class))
          
          .print("匿名函数");
        
        //
        executionEnvironment
          .addSource(new ClickSource_my())
          .flatMap(
            (ClickEvent_my in, Collector<ClickEvent_my> out) -> {
                out.collect(in);
            }
          )
          .returns(Types.POJO(ClickEvent_my.class)) // 自定义POJO时
          .print();
        //
        
        executionEnvironment.execute();
        
    }
    
}
