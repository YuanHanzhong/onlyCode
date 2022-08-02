package _02_exe;


import _01_tools.ClickSource;
import _01_tools.POJO_ClickEvent;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * // NOTE 2022/9/16 取出想要的东西
 */
public class _03_NameGet {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
    
        executionEnvironment.setParallelism(1);
    
        DataStreamSource<POJO_ClickEvent> pojo_clickEventDataStreamSource = executionEnvironment.addSource(new ClickSource());
        
        pojo_clickEventDataStreamSource.map(new MapFunction<POJO_ClickEvent, String>() {
            @Override
            public String map(POJO_ClickEvent value) throws Exception {
                return value.url;
            }
        })
          .print();
    
    
        executionEnvironment.execute();
        
    }
    
}
