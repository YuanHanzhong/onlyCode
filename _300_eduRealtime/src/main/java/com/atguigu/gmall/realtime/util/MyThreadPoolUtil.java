package com.atguigu.gmall.realtime.util;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Felix
 * Date: 2022/7/16
 * Desc: 线程池工具类
 * 双重校验锁解决单例设计模式懒汉式线程安全问题
 */
public class MyThreadPoolUtil {
    private static volatile ThreadPoolExecutor threadPoolExecutor;

    public static ThreadPoolExecutor getInstance(){
        if(threadPoolExecutor == null){
            synchronized(MyThreadPoolUtil.class){
                if(threadPoolExecutor == null){
                    System.out.println("~~开辟线程池~~");
                    threadPoolExecutor = new ThreadPoolExecutor(
                        4,20,300, TimeUnit.SECONDS,
                      new LinkedBlockingDeque<Runnable>(Integer.MAX_VALUE)
                    );
                }
            }
        }
        return threadPoolExecutor;
    }
}
