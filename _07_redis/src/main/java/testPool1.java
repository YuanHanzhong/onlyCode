import redis.clients.jedis.Jedis;

public class testPool1 {
    public static void main(String[] args) throws InterruptedException {
        // 连续申请2次资源会报错, 符合预期
        Jedis resource = MyJedisPool.getJedisPool().getResource();
        //Jedis resource2 = MyJedisPool.getJedisPool().getResource();
        System.out.println("操作前元素: " + resource.hgetAll("hk1"));
    
        // 接下来的50s里会一直进行操作, 保证连接没有丢掉
        for (int i = 0; i < 10; i++) {
            System.out.println("操作中: " + resource.hincrBy("hk1","hf1",1));
            Thread.sleep(5000);
        }
        System.out.println("操作后: " + resource.hgetAll("hk1"));
    }
    
}
