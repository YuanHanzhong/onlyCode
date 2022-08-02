package com.atguigu.gmall.realtime.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.common.MyGmallConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.util.List;

/**

 * Desc: 查询维度的工具类
 */
public class MyDimUtil {
    public static JSONObject getDimInfo(Connection conn, String tableName,String id){
        return getDimInfo(conn,tableName,Tuple2.of("ID",id));
    }
    /**
     * 查询维度数据，使用旁路缓存
     *      思路：先从缓存中查询维度，如果缓存中找到了，直接将缓存中的维度作为返回值返回(缓存命中)；
     *              如果在缓存中没有找到对应的维度信息，需要发送请求到Phoenix表中查询维度，
     *              并且将查询的结果放到缓存中缓存起来，下次直接从缓存中获取即可。
     *      缓存选型：
     *           Flink状态: 性能好，但是只能当前进程中对其进行访问，操作性差
     *           Redis:    性能也不错，操作方便
     *      注意：
     *         缓存的数据应该设置expire，不能常驻Redis
     *         一旦业务系统的维度数据发生了变化，缓存中的数据也应该清空
     *       分析：
     *          type：       String
     *          key命名:     dim:维度表名:主键值1_主键值2
     */
    public static JSONObject getDimInfo(Connection conn, String tableName, Tuple2<String, String> ... columnNameAndValues) {
        //拼接到Redis中查询维度数据的key
        StringBuilder redisKey = new StringBuilder("dim:" + tableName.toLowerCase() + ":");
        //拼接查询语句
        StringBuilder selectSql = new StringBuilder("select * from " + MyGmallConfig.PHOENIX_SCHEMA + "." + tableName + " where ");
        for (int i = 0; i < columnNameAndValues.length; i++) {
            Tuple2<String, String> columnNameAndValue = columnNameAndValues[i];
            String columnName = columnNameAndValue.f0;
            String columnValue = columnNameAndValue.f1;
            redisKey.append(columnValue);
            selectSql.append(columnName + "='" + columnValue + "'");
            if (i < columnNameAndValues.length - 1) {
                redisKey.append("_");
                selectSql.append(" and ");
            }
        }

        //先从缓存中查询维度
        Jedis jedis = null;
        String dimInfoStr = null;
        JSONObject dimJsonObj = null;

        try {
            jedis = MyRedisUtil.getJedis();
            dimInfoStr = jedis.get(redisKey.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("从Redis缓存中查询数据发生了异常");
        }

        if(StringUtils.isNotEmpty(dimInfoStr)){
            //如果缓存中找到了，直接将缓存中的维度作为返回值返回(缓存命中)
            dimJsonObj = JSON.parseObject(dimInfoStr);
        }else{
            //如果在缓存中没有找到对应的维度信息，需要发送请求到Phoenix表中查询维度
            System.out.println("~~从phoenix表中查询维度的SQL:" + selectSql);
            //注意：查询维度的时候，底层还是调用的PhoenixUtil工具类中的从表中查询数据的方法
            List<JSONObject> jsonObjList = MyPhoenixUtil.queryList(conn, selectSql.toString(), JSONObject.class);

            //判断是否存在对应的维度数据
            if (jsonObjList != null && jsonObjList.size() > 0) {
                //如果集合不为null，那么集合中也只会有一个元素，因为我们在查询的时候，我们是根据维度的主键进行查询的
                dimJsonObj = jsonObjList.get(0);
                //并且将查询的结果放到缓存中缓存起来，下次直接从缓存中获取即可。
                if(jedis != null){
                    jedis.setex(redisKey.toString(),3600*24,dimJsonObj.toJSONString());
                }
            } else {
                System.out.println("~~要查询的维度数据没找到:" + selectSql);
            }
        }

        //关闭Redis连接
        if(jedis != null){
            System.out.println("~~关闭Jedis客户端~~");
            jedis.close();
        }

        return dimJsonObj;
    }
    //查询维度数据 没有使用优化
    public static JSONObject getDimInfoNoCache(Connection conn, String tableName, Tuple2<String, String>... columnNameAndValues) {
        //拼接查询语句
        StringBuilder selectSql = new StringBuilder("select * from " + MyGmallConfig.PHOENIX_SCHEMA + "." + tableName + " where ");
        for (int i = 0; i < columnNameAndValues.length; i++) {
            Tuple2<String, String> columnNameAndValue = columnNameAndValues[i];
            String columnName = columnNameAndValue.f0;
            String columnValue = columnNameAndValue.f1;
            selectSql.append(columnName + "='" + columnValue + "'");
            if (i < columnNameAndValues.length - 1) {
                selectSql.append(" and ");
            }
        }
        System.out.println("~~从phoenix表中查询维度的SQL:" + selectSql);
        //注意：查询维度的时候，底层还是调用的PhoenixUtil工具类中的从表中查询数据的方法
        List<JSONObject> jsonObjList = MyPhoenixUtil.queryList(conn, selectSql.toString(), JSONObject.class);

        JSONObject dimJsonObj = null;
        //判断是否存在对应的维度数据
        if (jsonObjList != null && jsonObjList.size() > 0) {
            //如果集合不为null，那么集合中也只会有一个元素，因为我们在查询的时候，我们是根据维度的主键进行查询的
            dimJsonObj = jsonObjList.get(0);
        } else {
            System.out.println("~~要查询的维度数据没找到:" + selectSql);
        }
        return dimJsonObj;
    }

    public static void delCached(String tableName,String id){
        String redisKey = "dim:"+tableName.toLowerCase()+":"+id;
        Jedis jedis = null;
        try {
            jedis = MyRedisUtil.getJedis();
            jedis.del(redisKey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }
    public static void main(String[] args) throws Exception {
        DruidDataSource dataSource = MyDruidDSUtil.createDataSource();
        DruidPooledConnection conn = dataSource.getConnection();
        //JSONObject dimJsonObj = getDimInfoNoCache(conn, "dim_base_trademark", Tuple2.of("id", "33"));
        //JSONObject dimJsonObj = getDimInfo(conn, "dim_base_trademark", Tuple2.of("id", "3"));
        JSONObject dimJsonObj = getDimInfo(conn, "dim_base_trademark", "3");
        System.out.println(dimJsonObj);
    }
}
