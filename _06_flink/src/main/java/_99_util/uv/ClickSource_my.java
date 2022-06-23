package _99_util.uv;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Calendar;
import java.util.Random;
/*
需求: 模拟产生数据
    1. implements SourceFunction
    2. 泛型为 <POJO>
    3. 造数组
         username--> userArray
         url--> urlArray
    4. 造flag
    

 
 */

public class ClickSource_my implements SourceFunction<ClickEvent_my>{
    
    // 造种子, 定义结构
    String[] userArray = {"hello", "world", "flink"};
    String[] urlArray = {"/cart", "/bug"};
    
    // 随机
    Random random = new Random();
    
    private boolean isRunning=true;
    
    @Override
    public void run(SourceContext<ClickEvent_my> ctx) throws Exception {
        while (isRunning) {
            ctx.collect(
              new ClickEvent_my(
                userArray[random.nextInt(userArray.length)],
                urlArray[random.nextInt(urlArray.length)],
                Calendar.getInstance().getTimeInMillis()
              )
            );
            
        }
        Thread.sleep(1000);
    
    }
    
    @Override
    public void cancel() {
        isRunning=false;
    
    }
}
