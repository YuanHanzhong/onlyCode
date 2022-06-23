package _08_richFunction;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.Random;

/**
 * 使用keyedprocessFunction实现, 计算截止到目前的最大值, 最小值, 总和, 总条数, 平均值
 */
public class _05_keyedToReduce {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setParallelism(1);
    
        executionEnvironment
          .addSource(new SourceFunction<Integer>() {
              private boolean isRuning = true;
              private Random random = new Random();
          
              @Override
              public void run(SourceContext<Integer> ctx) throws Exception {
                  while (isRuning) {
                      ctx.collect(random.nextInt(10));
                      Thread.sleep(1000L);
                  }
              }
          
          
              @Override
              public void cancel() {
              
                  isRuning = false;
              }
          })
          .map(r -> Tuple5.of( // keyBy 相当于shuffle作用, map-->keyBy-->reduce
            r,
            r,
            r,
            1,
            r
          ))
          .returns(Types.TUPLE(
            Types.INT,
            Types.INT,
            Types.INT,
            Types.INT,
            Types.INT
          ))
          .keyBy(r -> "int") // 不能直接跟random , 怎么理解? got
          //.reduce(new ReduceFunction<Tuple5<Integer, Integer, Integer, Integer, Integer>>() {
          //    @Override
          //    public Tuple5<Integer, Integer, Integer, Integer, Integer> reduce(Tuple5<Integer, Integer, Integer, Integer, Integer> in, Tuple5<Integer, Integer, Integer, Integer, Integer> acc) throws Exception {
          //        return Tuple5.of(
          //          Math.max(in.f0, acc.f0),
          //          Math.min(in.f1, acc.f1),
          //          in.f2+acc.f2,
          //          in.f3+acc.f3,
          //          (in.f2+acc.f2)/(in.f3+acc.f3)
          //        );
          //    }
          //})
          
          .process(new KeyedProcessFunction<String, Tuple5<Integer, Integer, Integer, Integer, Integer>, Tuple5<Integer, Integer, Integer, Integer, Integer>>() {
    
              @Override
              public void processElement(
                Tuple5<Integer, Integer, Integer, Integer, Integer> value,
                Context ctx,
                Collector<Tuple5<Integer, Integer, Integer, Integer, Integer>> out) throws Exception {
                
        
              }
          })
          .print();
        
        executionEnvironment.execute();
    }
    
}
