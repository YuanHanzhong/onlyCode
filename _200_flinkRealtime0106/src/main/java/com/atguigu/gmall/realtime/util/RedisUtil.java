package com.atguigu.gmall.realtime.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Author: Felix
 * Date: 2022/7/16
 * Desc: 操作Redis的工具类
 */
public class RedisUtil {
    private static JedisPool jedisPool;

    public static void initJedisPool(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMinIdle(5);
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(2000);
        poolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(poolConfig,"hadoop102",6379,10000);
    }
    public static Jedis getJedis(){
        if(jedisPool == null){
            initJedisPool();
        }
        System.out.println("~~开启Jedis客户端~~");
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

    public static void main(String[] args) {
        Jedis jedis = getJedis();
        String pong = jedis.ping();
        System.out.println(pong);
    }
}
