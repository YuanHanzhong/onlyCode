package com.atguigu.gmall.realtime.util;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Felix
 * Date: 2022/6/2
 * Desc: 获取线程池工具类
 */
public class ThreadPoolUtil {
    private static volatile ThreadPoolExecutor poolExecutor;

    public static ThreadPoolExecutor getInstance(){
        if(poolExecutor == null){
            synchronized (ThreadPoolUtil.class){
                if(poolExecutor == null){
                    System.out.println("~~~开辟线程池~~~");
                    poolExecutor = new ThreadPoolExecutor(4,
                        20,
                        300,
                        TimeUnit.SECONDS,
                        new LinkedBlockingDeque<Runnable>(Integer.MAX_VALUE));
                }
            }
        }
        return poolExecutor;
    }
}
