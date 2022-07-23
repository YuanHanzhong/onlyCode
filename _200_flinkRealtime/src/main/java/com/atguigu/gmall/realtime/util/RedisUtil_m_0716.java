package com.atguigu.gmall.realtime.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Author: Felix
 * Date: 2022/6/2
 * Desc: 获取Jedis的工具类
 */
public class RedisUtil_m_0716 {
    private static JedisPool jedisPool;
    public static void initJedisPool(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        
        // 常规设置
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxIdle(5); // 2022/7/16 9:12 NOTE 空闲的, 大于5个后, 销毁到 min
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(2000);
        poolConfig.setTestOnBorrow(true);
        
        /*
         2022/7/16 11:02, NOTE 对连接池的理解
            实际      池中
            3         5
            8         8
            96        96
            105       100, 有5个等待
            100       100
            96        100, 还不触发MaxIde, 所以仍然为100
            95        95
            20        20
            5         5
            2         5
            1         5
            
        */
        
        jedisPool = new JedisPool(poolConfig,"hadoop102",6379,10000);
    }
    
    // 2022/7/16 21:01 NOTE P1 Jedis也用2个懒汉锁实现
    
    // 懒汉式? P3 多线程时, 不是真单例
    public static Jedis getJedis(){
        if(jedisPool == null){ // NOTE pool是重量级的, 没有再创建
            initJedisPool();
        }
        System.out.println("~~~获取Jedis客户端~~~");
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
    
    // 2022/7/16 9:50 NOTE  单元测试, 步步为营
    public static void main(String[] args) {
        Jedis jedis = getJedis();
        String pong = jedis.ping();
        System.out.println(pong);
    }
}
