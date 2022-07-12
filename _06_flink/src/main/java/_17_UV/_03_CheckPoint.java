package _17_UV;

import com.atguigu.utils.ClickEvent;
import com.atguigu.utils.ClickSource;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _03_CheckPoint {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env.enableCheckpointing(1 * 1000L);
        // 设置检查点文件夹的绝对路径
        // file:// + 文件夹的绝对路径, windows的路径为反斜杠
    
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("file:\\D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_06_flink\\src\\main\\resources\\checkpoints\\checkpoint"); // 这里默认为文件夹, 不是具体的文件
    
        DataStreamSource<ClickEvent> clickEventDataStreamSource = env.addSource(new ClickSource());
    
        SingleOutputStreamOperator<Tuple2<ClickEvent, Integer>> mappedStream = clickEventDataStreamSource.flatMap(new FlatMapFunction<ClickEvent, Tuple2<ClickEvent, Integer>>() {
            @Override
            public void flatMap(ClickEvent value, Collector<Tuple2<ClickEvent, Integer>> out) throws Exception {
                out.collect(Tuple2.of(new ClickEvent(),1));
            
            }
        });
    
        KeyedStream<Tuple2<ClickEvent, Integer>, String> keyedStream = mappedStream.keyBy(new KeySelector<Tuple2<ClickEvent, Integer>, String>() {
            @Override
            public String getKey(Tuple2<ClickEvent, Integer> value) throws Exception {
                return value.f0.username;
            }
        });
    
        SingleOutputStreamOperator<Tuple2<ClickEvent, Integer>> result = keyedStream.sum(1);
    
    
        result.print();
    
    
    
        env.execute();
    }
}
