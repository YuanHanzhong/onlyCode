package com.atguigu.prome.func;


import com.atguigu.prome.util.DimUtil;
import com.atguigu.prome.util.ThreadPoolUtil;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;


import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.lang.String;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: zhangchen
 * Date: 2022/8/1
 * Desc: 发送异步请求进行维度关联
 */
public   class DimAsyncFunction extends RichAsyncFunction<String, String>   {
    private ExecutorService executorService;

    Counter hitCounter=null;
    Counter totalCounter=null;

    Lock hitLock=new ReentrantLock();
    Lock totalLock=new ReentrantLock();

    @Override
    public void open(Configuration parameters) throws Exception {
        //初始化线程池对象
        System.out.println("open ");
        executorService = ThreadPoolUtil.getInstance();

        hitCounter= getRuntimeContext().getMetricGroup().addGroup("Cache").counter("HitCounter");
        totalCounter  = getRuntimeContext().getMetricGroup().addGroup("Cache").counter("TotalCounter");

    }

    @Override
    public void asyncInvoke(String key, ResultFuture<String> resultFuture) throws Exception {
        // 开启多个线程，发送异步请求
        executorService.submit(
            new Runnable() {
                @Override
                public void run() {
                    //返回值为 tuple(查询值,是否命中缓存)
                    //System.out.println("线程名称 = " + Thread.currentThread().getName());
                    Tuple2<String, Boolean> valueTuple = DimUtil.getDimInfo(key);
                    String dimValue = valueTuple.f0;
                    String result= key+"_"+dimValue;
                    resultFuture.complete(Collections.singleton(result));

                    Boolean ifHit = valueTuple.f1;
                    if(ifHit){
                        try{
                            hitLock.lock();
                            hitCounter.inc();
                        }finally {
                            hitLock.unlock();
                        }
                    }
                    try {
                        totalLock.lock();
                        totalCounter.inc();
                    }finally {
                        totalLock.unlock();
                    }

                }
            }
        );
    }

}
