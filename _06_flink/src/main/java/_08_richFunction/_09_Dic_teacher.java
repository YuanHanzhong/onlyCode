package _08_richFunction;

import com.atguigu.utils.ClickEvent;
import com.atguigu.utils.ClickSource;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class _09_Dic_teacher {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        env
          .addSource(new ClickSource())
          .keyBy(r -> r.username)
          .process(new UserUrlStatistics())
          .print();
        
        env.execute();
    }
    
    public static class UserUrlStatistics extends KeyedProcessFunction<String, ClickEvent, String> {
        // key: url
        // value: count
        private MapState<String, Integer> urlCount;
        
        @Override
        public void open(Configuration parameters) throws Exception {
            urlCount = getRuntimeContext().getMapState(
              new MapStateDescriptor<String, Integer>(
                "url-count",
                Types.STRING, // key
                Types.INT // value
              )
            );
        }
        
        @Override
        public void processElement(ClickEvent in, Context ctx, Collector<String> out) throws Exception {
            // urlCount.contains检查的是in.username对应的字典状态变量中是否
            // 包含in.url这个key
            if (!urlCount.contains(in.url)) {
                // 用户in.username第一次访问in.url
                // 写入的是in.username对应的字典状态变量
                urlCount.put(in.url, 1);
            } else {
                // 访问量加一
                Integer oldCount = urlCount.get(in.url);
                Integer newCount = oldCount + 1;
                urlCount.put(in.url, newCount);
            }
            
            StringBuilder result = new StringBuilder();
            result.append(ctx.getCurrentKey() + " {\n");
            // 遍历in.username对应的字典状态变量的所有的key
            for (String key : urlCount.keys()) {
                result.append("  ")
                  .append("\"" + key + "\" -> ")
                  .append(urlCount.get(key) + ",\n");
            }
            result.append("}\n");
            
            out.collect(result.toString());
        }
    }
}
