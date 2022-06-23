package _15_CoStream;

import com.atguigu.utils.ClickEvent;
import com.atguigu.utils.ClickSource;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;

/*
需求: 查询流, 实现查询功能
分析:
    查询的数据源来自nc
    源数据来自ClickSource
    
    clicksource.setP(1).broadCast,

 */
// 查询流
public class _01_QuaryCoStream_my {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        DataStreamSource<ClickEvent> clickSource = env.addSource(new ClickSource());
        
        DataStreamSource<String> queryStream = env.socketTextStream("hadoop102", 9999);
        
        clickSource.keyBy(r -> r.username)
          // 数据流在广播之前，并行度一定要设置成1
          // 因为数据需要按照顺序广播出去
          // 为了使下游的并行子任务看到的都是相同的广播数据, GOT 要想相同, 必须有序
          .connect(queryStream.setParallelism(1).broadcast())
          .flatMap(new CoFlatMapFunction<ClickEvent, String, ClickEvent>() {
              // 用来保存socket输入的查询字符串，初始值为空
              private String queryString = "";
              
              @Override
              public void flatMap1(ClickEvent in1, Collector<ClickEvent> out) throws Exception {
                  if (in1.username.equals(queryString))
                      out.collect(in1);
              }
              
              @Override
              public void flatMap2(String in2, Collector<ClickEvent> out) throws Exception {
                  // socket的数据进入flatMap算子触发flatMap2的调用
                  // 将socket输入的字符串保存在queryString中
                  queryString = in2;
              }
          })
          .print();
        
        env.execute();
    }
}
