import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
public  class MyJedisPool {
    public static JedisPool jedisPool = null;
    
    public static JedisPool getJedisPool() {
    
        if (jedisPool == null) {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(1);
            // idle 表示随时待命,
            // setMinIdle能保证需要时立刻连上, 省去了申请新连接的时间
            jedisPoolConfig.setMinIdle(1);
            // setMaxIdle是为了省资源
            jedisPoolConfig.setMaxIdle(1);
            jedisPoolConfig.setBlockWhenExhausted(false);
            jedisPoolConfig.setTestOnBorrow(true);
    
            jedisPool = new JedisPool(jedisPoolConfig, "hadoop102", 6379);
        }
        return jedisPool;
    }
    
    
}
