package com.atguigu.gmall.realtime.app.func;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.util.DimUtil;
import com.atguigu.gmall.realtime.util.DruidDSUtil;
import com.atguigu.gmall.realtime.util.ThreadPoolUtil;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

/**
 * Author: Felix
 * Date: 2022/6/6
 * Desc: 发送异步请求
 * 模板方法设计模式；
 *      在父类中定义完成某一天功能的核心算法的骨架(步骤)，具体的实现延迟到子类中去完成
 *      在不改变父类核心算法骨架的前提下，每一个子类都可以有自己不同的实现
 */
public abstract class DimAsyncFunction<T> extends RichAsyncFunction<T, T> implements DimJoinFunction<T>{

    private ExecutorService executorService;
    private DruidDataSource dataSource;

    private String tableName;

    public DimAsyncFunction(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        executorService = ThreadPoolUtil.getInstance();
        dataSource = DruidDSUtil.createDataSource();
    }

    @Override
    public void asyncInvoke(T obj, ResultFuture<T> resultFuture) throws Exception {
        //开启多个线程，发送异步请求
        executorService.submit(
            new Runnable() {
                @Override
                public void run() {
                    Connection conn = null;
                    try {
                        //1.从流中的对象上获取要关联的维度的主键
                        String key = getKey(obj);
                        //2.根据主键到维度表中获取维度对象
                        conn = dataSource.getConnection();
                        JSONObject dimInfoJsonObj = DimUtil.getDimInfo(conn, tableName, key);
                        //3.将维度对象的属性补充到流中的对象上
                        if(dimInfoJsonObj != null){
                            join(obj,dimInfoJsonObj);
                        }
                        //获取数据库交互的结果并发送给ResultFuture的回调函数
                        resultFuture.complete(Collections.singleton(obj));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if(conn != null){
                            try {
                                System.out.println("~~~phoenix连接被关闭~~~");
                                conn.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        );
    }
}
