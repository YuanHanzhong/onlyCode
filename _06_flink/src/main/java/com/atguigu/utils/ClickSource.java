package com.atguigu.utils;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Calendar;
import java.util.Random;


public class ClickSource implements SourceFunction<ClickEvent>{
    
    private boolean running = true;
    private Random random = new Random();
    private String[] userArray = {"Mary", "Bob", "Alice"};
    private String[] urlArray = {"./home", "./cart", "./buy"};
    @Override
    public void run(SourceFunction.SourceContext<ClickEvent> ctx) throws Exception {
        while (running) {
            ctx.collect(new ClickEvent(
              userArray[random.nextInt(userArray.length)],
              urlArray[random.nextInt(urlArray.length)],
              // 获取当前的机器时间，作为事件的事件时间
              Calendar.getInstance().getTimeInMillis()
            ));
            Thread.sleep(1000L);
        }
    }
    
    @Override
    public void cancel() {
        running = false;
    }
}
