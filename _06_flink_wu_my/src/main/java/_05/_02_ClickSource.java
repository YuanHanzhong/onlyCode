package _05;

/*
随心所欲产生数据

 */

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Calendar;
import java.util.Random;

public class _02_ClickSource implements SourceFunction {
    
    private Boolean running = true;
    
    /**
     * 目的: 一直产生数据
     * @param ctx
     * @throws Exception
     */
    @Override
    public void run(SourceContext ctx) throws Exception {
        Random random = new Random();
    
        String[] users = {"Tom", "Jack", "Mike", "Jason"};
        String[] urls = {"./home", "./cart", "/fav"};
    
        while (running) {
        
            // NOTE collect 得有个收集模板, 所以先定义一个 pojo 类
            ctx.collect(new _01_EventPojo(
              users[random.nextInt(users.length)],
              urls[random.nextInt(urls.length)],
              Calendar.getInstance().getTimeInMillis()// NOTE 获取时间戳
            ));
            
            // NOTE 为了产生的慢点, 使用 sleep
            Thread.sleep(1000L);
        }
        
    }
    
    @Override
    public void cancel() {
        running = false;
    }
}

/**
 * 目的: 要用 mac?
 *      好处:
 *          更接近真是的开发环境
 *          键盘更好用
 *          看文档更方便, 这个功能不可替代
 *      坏处:
 *          费流量
 *          把转接器拿过来
 *          打开阿里云环境, 否则总是碍手碍脚
*       TODO:
 *          实现项目, 哪里不会重点补充哪里
 *          1.
 *              读完文档
*           2. 练着玩
 */

/*
TODO
    这里应该可以用快捷键
Todo 的话, 没有必要分开, 就是小的模块, 随时想到随时速记

 */
