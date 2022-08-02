package _01_HelloWorld;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class _01_WordCountNet_m_0710 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    
        env.enableCheckpointing(1000L, CheckpointingMode.EXACTLY_ONCE);
        // 2022/7/10 18:53 NOTE GOT 每个应用都建立一个新的checkpoint目录比较好, 容易区分
    
        ////TODO 2.检查点相关的设置
        ////2.1 开启检查点
        //env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        ////2.2 检查点超时时间
        //env.getCheckpointConfig().setCheckpointTimeout(60000L);
        ////2.3 job取消之后，检查点是否保留
        //env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        ////2.4 两个检查点之间最小时间间隔
        //env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000L);
        ////2.5 设置重启策略
        ////env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000L));
        //env.setRestartStrategy(RestartStrategies.failureRateRestart(3, Time.days(30), Time.seconds(3)));
        ////2.6 设置状态后端
        //env.setStateBackend(new HashMapStateBackend());
        ////env.getCheckpointConfig().setCheckpointStorage(new JobManagerCheckpointStorage());
        //env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/gmall/ck");
        //System.setProperty("HADOOP_USER_NAME","atguigu");
        //
        DataStreamSource<String> sourceNet = env.socketTextStream("localhost", 9999); // nc -l 9999 来模拟产生数据;
    
    
    
        // mappedStream
        SingleOutputStreamOperator<Tuple2<String, Integer>> mappedStream = sourceNet.flatMap(
          new FlatMapFunction<String, Tuple2<String, Integer>>() {
              @Override
              public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                  String[] s = value.split(" ");
                  for (String word : s) {
                      out.collect(Tuple2.of(word, 1));
                  }
              
              }
          }
        );
    
        KeyedStream<Tuple2<String, Integer>, String> keyedStream = mappedStream.keyBy(k -> k.f0);
        SingleOutputStreamOperator<Tuple2<String, Integer>> result = keyedStream.sum("f1");
        
        result.print();
        
        env.execute();
    
    }
    
}
