package com.atguigu.gmall.realtime.util;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil_m_0716_v1 {
    private static volatile ThreadPoolExecutor poolExecutor;
    
    public static ThreadPoolExecutor getInstance() {
        // 2022/7/16 21:26 NOTE  外层if是为了高效, 不做无用功
        if (poolExecutor == null) {
    
            // 2022/7/16 21:26 NOTE 内层if, 在锁子之内, 确保单线程
            synchronized (ThreadPoolUtil_m_0716_v1.class) {
                if (poolExecutor == null) { // 2022/7/16 21:21 NOTE 双重校验锁解决懒汉式线程安全问题
                    System.out.println("~~~开辟线程池~~~");
                    poolExecutor = new ThreadPoolExecutor(
                      4,
                      20,
                      300,
                      TimeUnit.SECONDS,
                      new LinkedBlockingDeque<Runnable>(Integer.MAX_VALUE)); // 没有可用的, 等待
                }
            }
        }
        return poolExecutor;
    }
}

//public class ThreadPoolUtil_{
//    private static volatile ThreadPoolExecutor poolExecutor;
//
//    public static ThreadPoolExecutor getInstance() {
//        if (poolExecutor == null) {
//            synchronized (ThreadPoolUtil_.class) {
//                if (poolExecutor == null) {
//                    poolExecutor = new ThreadPoolExecutor(
//                      10,
//                      100,
//                      100L,
//                      TimeUnit.SECONDS,
//                      new LinkedBlockingDeque<Runnable>(Integer.MAX_VALUE)
//
//                    );
//                }
//            }
//        }
//        return poolExecutor;
//    }
    
//}
