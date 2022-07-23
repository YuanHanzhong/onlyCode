package com.atguigu.gmall.realtime.util;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 Desc: 获取线程池工具类
 
 开启线程, 用start
 真正执行时, 用run
 
 // 2022/7/16 11:31 NOTE 单例模式, 懒汉式比较省空间. 需要时反应慢点.
 */


public class ThreadPoolUtil_m_0716 {
    private static volatile ThreadPoolExecutor poolExecutor; // 2022/7/16 11:38 NOTE volatile, 修改了立马让其他知道
    // 2022/7/16 11:33 NOTE 对象锁
    //public static synchronized ThreadPoolExecutor getInstance() {
    
    public static ThreadPoolExecutor getInstance() {
        if (poolExecutor == null) { // 2022/7/16 11:36 NOTE 价格判断, 很高明. 不做无用功. STAR
            // 解决线程安全问题, 加锁, 只能一个进来
            synchronized (ThreadPoolUtil_m_0716.class) { // 2022/7/16 11:34 NOTE 锁加在代码块上好, 加锁的代码越少效率越高
                if (poolExecutor == null) { // 2022/7/16 11:37 NOTE 这个有必要保留, **双重校验锁**
                    System.out.println("~~~开辟线程池~~~");
                    poolExecutor = new ThreadPoolExecutor(
                      4,
                      20,
                      300,
                      TimeUnit.SECONDS,
                      // 保证里面都能run, Runable
                      new LinkedBlockingDeque<Runnable>(Integer.MAX_VALUE)); // 没有可用的, 等待
                }
            }
        }
        return poolExecutor;
    }
}
