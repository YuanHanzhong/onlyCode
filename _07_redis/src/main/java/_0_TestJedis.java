import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ListPosition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class _0_TestJedis {
    public static void main(String[] args) throws InterruptedException {
        JedisPool jedisPool = MyJedisPool.getJedisPool();
    
        Jedis resource = jedisPool.getResource();
    
        System.out.println("resource.ping(\"hello\") = " + resource.ping("hello"));
    
        /**
         * exercise keys
         */
    
    
        // set
        System.out.println("resource.set(\"sk1\", \"sv1\") = " + resource.set("sk1", "sv1"));
        System.out.println("resource.set(\"sk2\", \"sv2\") = " + resource.set("sk2", "sv2"));
        System.out.println("resource.set(\"sk3\", \"sv3\") = " + resource.set("sk3", "sv3"));
    
        // expire
        System.out.println("resource.expire(\"sk1\", 10) = " + resource.expire("sk1", 10));
        
        // get
        System.out.println("resource.get(\"sk2\") = " + resource.get("sk2"));
    
        // delete
        System.out.println("resource.del(\"sk2\") = " + resource.del("sk2"));
        
        // exists
        System.out.println("resource.exists(\"sk2\") = " + resource.exists("sk2"));
        System.out.println("resource.exists(\"sk2\",\"sk3\") = " + resource.exists("sk2", "sk3"));
        System.out.println("resource.exists(\"sk3\") = " + resource.exists("sk3"));
        
        // type
        System.out.println("resource.type(\"sk1\") = " + resource.type("sk1"));
        System.out.println("resource.type(\"sk11\") = " + resource.type("sk11"));
    
        // dbsizes
        System.out.println("resource.dbSize() = " + resource.dbSize());
    
        // ttl
        System.out.println("resource.ttl(\"sk1\") = " + resource.ttl("sk1"));
        //Thread.sleep(5000); // 睡5秒, milli是1000分之一
        System.out.println("resource.ttl(\"sk1\") = " + resource.ttl("sk1"));
    
        /**
         * exercise string
         */
    
        // append
        System.out.println("resource.append(\"sk1\", \"add somthing\") = " + resource.append("sk1", "add somthing"));
        
        // strlen
        System.out.println("resource.strlen(\"sk1\") = " + resource.strlen("sk1"));
        System.out.println("resource.strlen(\"sk3\") = " + resource.strlen("sk3"));
        
        // setnx
        System.out.println("resource.setnx(\"sk4\", \"sv4\") = " + resource.setnx("sk4", "sv4"));
        System.out.println("resource.get(\"sk4\") = " + resource.get("sk4"));
        System.out.println("resource.setnx(\"sk3\", \"sk3 already exists\") = " + resource.setnx("sk3", "sk3 already exists"));
        
        // incr, 需要为纯数字串
        System.out.println("resource.incrBy(\"sk5\", 5) = " + resource.incrBy("sk5", 5));
        System.out.println("resource.incrBy(\"sk5\", 5) = " + resource.incrBy("sk5", 5));
        System.out.println("resource.incrBy(\"sk5\", 3) = " + resource.incrBy("sk5", 3));
        System.out.println("resource.get(\"sk5\") = " + resource.get("sk5"));
        
        // decr
        resource.decr("sk5");
        resource.decr("sk5");
        resource.decr("sk5");
        System.out.println("resource.get(\"sk5\") = " + resource.get("sk5"));
    
        // mset
        System.out.println("resource.msetnx(\"sk6\",\"sv6\",\"sk7\",\"sv7\") = " + resource.msetnx("sk6", "sv6", "sk7", "sv7"));
        
        // mget
        System.out.println("resource.mget(\"sk6\", \"sk7\") = " + resource.mget("sk6", "sk7"));
        
        // getrange
        System.out.println("resource.getrange(\"sk1\", 0, -1) = " + resource.getrange("sk1", 0, -1));
        
        // setrange
        System.out.println("resource.setrange(\"sk1\", 1, \"23\") = " + resource.setrange("sk1", 1, "23"));
        System.out.println("resource.getrange(\"sk1\", 0, -1) = " + resource.getrange("sk1", 0, -1));
        
        // getset
        System.out.println("resource.getSet(\"sk1\", \"hello\") = " + resource.getSet("sk1", "hello"));
        System.out.println("resource.get(\"sk1\") = " + resource.get("sk1"));
    
    
        Set<String> keys = resource.keys("*");
        System.out.println("keys = " + keys);
        resource.close();
    }
    
    @Test
    public void testString() {
        Jedis resource = MyJedisPool.getJedisPool().getResource();
        
        // 测试连接池, 是不是在原来的基础上增加
        //resource.set("sk1", "1");
        System.out.println("resource.incrBy(\"sk1\", 2) = " + resource.incrBy("sk1", 2));
        System.out.println("resource.incrBy(\"sk1\", 2) = " + resource.incrBy("sk1", 2));
        System.out.println("resource.incrBy(\"sk1\", 2) = " + resource.incrBy("sk1", 2));
    
        System.out.println("resource.keys(\"*\") = " + resource.keys("*"));
        resource.close();
    }
    
    
    
    
    
    
    
    
    @Test
    public void testList() {
        JedisPool jedisPool = MyJedisPool.getJedisPool();
    
        Jedis resource = jedisPool.getResource();
        
        //// lpush
        //System.out.println("resource.lpush(\"lk1\", \"lv1\", \"lv2\", \"lv3\", \"lv4\") = " + resource.lpush("lk1", "lv1", "lv2", "lv3", "lv4"));
        //
        //// lpop
        //System.out.println("resource.lpop(\"lk1\") = " + resource.lpop("lk1"));
        //System.out.println("resource.lpop(\"lk1\") = " + resource.lpop("lk1"));
        
        
        // lindex
        System.out.println("resource.lindex(\"lk1\", 3) = " + resource.lindex("lk1", 0));
        
        // llen
        System.out.println("resource.llen(\"lk1\") = " + resource.llen("lk1"));
        
        // linsert , star 参数填什么, 看提示
        System.out.println("resource.linsert(\"lk1\", 2, \"lv2\", \"llll\") = " + resource.linsert("lk1", ListPosition.BEFORE,"lv1","llllll"));
        
        // lrem , 从前往后删除, 只删除匹配上的元素
        System.out.println("resource.lrem(\"lk1\", 25, \"111111\") = " + resource.lrem("lk1", 1, "2"));
        // lrange
        System.out.println("resource.lrange(\"lk1\", 0, -1) = " + resource.lrange("lk1", 0, -1));
    
        System.out.println("resource.keys(\"*\") = " + resource.keys("*"));
        resource.close();
    
    }
    
    @Test
    public void testSet() {
        Jedis resource = MyJedisPool.getJedisPool().getResource();
        
        // 添加元素, 有重复的会自动去掉
        System.out.println("resource.sadd(\"setk1\", \"1\", \"2\", \"3\", \"2\") = " + resource.sadd("setk1", "1", "2", "3", "2","4","5","6"));
    
        // 列出所有元素
        System.out.println("resource.smembers(\"setk1\") = " + resource.smembers("setk1"));
        
        // 看是否为成员
        System.out.println("resource.sismember(\"setk1\", \"4\") = " + resource.sismember("setk1", "4"));
        System.out.println("resource.sismember(\"setk1\", \"2\") = " + resource.sismember("setk1", "2"));
    
        // 元素个数
        System.out.println("resource.scard(\"setk1\") = " + resource.scard("setk1"));
        
        // 删除某元素
        System.out.println("resource.srem(\"setk1\", \"2\") = " + resource.srem("setk1", "2"));
        System.out.println("resource.smembers(\"setk1\") = " + resource.smembers("setk1"));
        
        // 随机取一个
        System.out.println("resource.spop(\"setk1\") = " + resource.spop("setk1"));
        System.out.println("resource.smembers(\"setk1\") = " + resource.smembers("setk1"));
        
        // 随机取n个, 不删除
        System.out.println("resource.srandmember(\"setk1\",3) = " + resource.srandmember("setk1", 3));
        System.out.println("resource.smembers(\"setk1\") = " + resource.smembers("setk1"));
        
        // 复制
        System.out.println("resource.sunionstore(\"setk3\", \"setk2\") = " + resource.sunionstore("setk3", "setk2"));
    
    
        // 交集
        resource.srem("setk2", "5");
        System.out.println("resource.smembers(\"setk1\") = " + resource.smembers("setk1"));
        System.out.println("resource.smembers(\"setk2\") = " + resource.smembers("setk2"));
        System.out.println("resource.smembers(\"setk3\") = " + resource.smembers("setk3"));
        System.out.println("resource.sinter(\"setk1\",\"setk3\") = " + resource.sinterstore("setk4","setk1", "setk3")); // 成功返回1
        System.out.println("resource.smembers(\"setk4\") = " + resource.smembers("setk4"));
        
        
        // 差集
        System.out.println("resource.sdiff(\"setk1\",\"setk4\",\"setk2\") = " + resource.sdiff("setk1", "setk4", "setk2"));
        resource.close();
    }
    
    
    /**
     * testHash
     */
    @Test
    public void testHash() {
        Jedis resource = MyJedisPool.getJedisPool().getResource();
        // set, 分开设置
        System.out.println("resource.hset(\"hk1\", \"name\", \"jack\") = " + resource.hset("hk1", "name", "jack"));
        //System.out.println("resource.hset(\"hk1\", \"age\", \"20\") = " + resource.hset("hk1", "age", "20"));
        System.out.println("resource.hset(\"hk1\", \"age\", \"20\") = " + resource.hset("hk1", "birth", "1993"));
    
        // set, 一起设置, star
    
        Map<String, String> hash = new HashMap<String, String>();
    
        hash.put("a", "1");
        hash.put("b", "2");
        hash.put("c", "3");
        hash.put("d", "4");
        hash.put("e", "5");
        hash.put("f", "6");
        
        //
        resource.hmset("hk8", hash);
        System.out.println("resource.hgetAll(\"hk8\") = " + resource.hgetAll("hk8"));
    
        System.out.println("resource.hgetAll(\"hk8\") = " + resource.hgetAll("hk8"));
        
        // hexists
        System.out.println("resource.hexists(\"hk8\", \"name\") = " + resource.hexists("hk8", "name"));
        System.out.println("resource.hexists(\"hk1\", \"name\") = " + resource.hexists("hk1", "name"));
    
        System.out.println("resource.hincrBy(\"hk1\", \"age\", 2) = " + resource.hincrBy("hk1", "age", 2));
    
        System.out.println("resource.hvals(\"hk1\") = " + resource.hvals("hk1"));
        System.out.println("resource.hkeys(\"hk1\") = " + resource.hkeys("hk1"));
        resource.close();
    }
    
    @Test
    public void test() throws InterruptedException {
        
        // 连续申请2次资源会报错, 符合预期
        Jedis resource = MyJedisPool.getJedisPool().getResource();
    
        System.out.println("操作前元素: " + resource.hgetAll("hk1"));
    
        // 接下来的50s里会一直进行操作, 保证连接没有丢掉
        for (int i = 0; i < 10; i++) {
            System.out.println("操作中: " + resource.hincrBy("hk1","hf1",1));
            Thread.sleep(5000);
        }
        System.out.println("操作后: " + resource.hgetAll("hk1"));
    }
    
    @Test
    public void test2() throws InterruptedException {
        Jedis resource = MyJedisPool.getJedisPool().getResource();
    
        System.out.println("操作前元素: " + resource.hgetAll("hk1"));
    
        // 接下来的50s里会一直进行操作, 保证连接没有丢掉
        for (int i = 0; i < 10; i++) {
            System.out.println("操作中: " + resource.hincrBy("hk1","hf1",1));
            Thread.sleep(5000);
        }
        System.out.println("操作后: " + resource.hgetAll("hk1"));
    }
    
}
