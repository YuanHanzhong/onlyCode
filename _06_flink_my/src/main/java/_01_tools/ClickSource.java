package _01_tools;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Calendar;
import java.util.Random;


/**
 * clickSource 的目的就是不断产生 event
 */
public class ClickSource implements SourceFunction<POJO_ClickEvent> {
    
    Boolean isRunning=true;
    
    
    @Override
    public void run(SourceContext<POJO_ClickEvent> ctx) throws Exception {
        Random random = new Random();
    
        String[] urls = {"./cart", "./buy"};
        String[] names = {"Jack", "Mike", "Jeff"};
    
        while (isRunning) {
            ctx.collect(new POJO_ClickEvent(
              urls[random.nextInt(urls.length)],
              names[random.nextInt(names.length)],
              Calendar.getInstance().getTimeInMillis()// GOT 获取系统时间
              
            ));
            Thread.sleep(1000L);
    
        }
        
    }
    
    @Override
    public void cancel() {
        isRunning = false;
    }
}
