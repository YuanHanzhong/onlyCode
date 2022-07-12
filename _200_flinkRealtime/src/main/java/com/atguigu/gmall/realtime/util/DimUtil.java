package com.atguigu.gmall.realtime.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.common.GmallConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Author: Felix
 * Date: 2022/6/2
 * Desc: 查询维度的工具类
 */
public class DimUtil {
    public static JSONObject getDimInfo(Connection conn, String tableName, String id) {
        return getDimInfo(conn,tableName,Tuple2.of("ID",id));
    }

    /**
     * 查询维度数据
     * 优化：旁路缓存
     * 思路：先从缓存中查询维度数据，如果缓存中找到了对应的维度，直接将缓存中的维度数据作为方法的返回值，缓存命中；如果在缓存中，没有找到对应的维度信息，
     * 再发送请求到phoenix数据库中查询维度，并将查询出来的维度数据缓存起来。
     * 缓存产品选型
     * 状态 ：性能更好、维护不便
     * Redis：性能也不错、维护方便  √
     * Redis分析
     * 类型： String
     * TTL:  1day
     * key:  dim:维度表名:主键1_主键2
     */
    public static JSONObject getDimInfo(Connection conn, String tableName, Tuple2<String, String>... columnNameAndValues) {
        StringBuilder selectSql = new StringBuilder("select * from " + GmallConfig.PHOENIX_SCHEMA + "." + tableName + " where ");
        StringBuilder redisKey = new StringBuilder("dim:" + tableName.toLowerCase() + ":");

        for (int i = 0; i < columnNameAndValues.length; i++) {
            Tuple2<String, String> columnNameAndValue = columnNameAndValues[i];
            String columnName = columnNameAndValue.f0;
            String columnValue = columnNameAndValue.f1;
            selectSql.append(columnName + "='" + columnValue + "' ");
            redisKey.append(columnValue);
            if (i < columnNameAndValues.length - 1) {
                selectSql.append(" and ");
                redisKey.append("_");
            }
        }

        Jedis jedis = null;
        String jsonStr = null;
        JSONObject dimJsonObj = null;

        try {
            jedis = RedisUtil.getJedis();
            jsonStr = jedis.get(redisKey.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("~从Redis中查询维度数据发生了异常~");
        }

        if(StringUtils.isNotEmpty(jsonStr)){
            //缓存命中 直接将缓存中的维度数据作为方法的返回值
            dimJsonObj = JSON.parseObject(jsonStr);
        }else{
            //在redis中没有找打对应的维度数据    再发送请求到phoenix数据库中查询维度
            System.out.println("从phoenix表中查询数据的sql:" + selectSql);
            //到phoenix表中查询维度数据  底层还是调用PhoenixUtil
            List<JSONObject> jsonObjList = PhoenixUtil.queryList(conn, selectSql.toString(), JSONObject.class);
            //虽然调用PhoenixUtil工具类的查询方法返回的是List集合，但是我们在查询维度的时候，加了主键查询条件，所以如果维度存在，我们只会查询到一条结果
            if (jsonObjList != null && jsonObjList.size() > 0) {
                dimJsonObj = jsonObjList.get(0);
                //并将查询出来的维度数据缓存起来
                if(jedis != null){
                    jedis.setex(redisKey.toString(),3600*24,dimJsonObj.toJSONString());
                }
            } else {
                System.out.println("在phoenix表中，没有找到对应的维度数据:" + selectSql);
            }
        }


        //释放资源
        if(jedis != null){
            System.out.println("~~~关闭Jedis客户端~~~");
            jedis.close();
        }

        return dimJsonObj;
    }

    public static JSONObject getDimInfoNoCache(Connection conn, String tableName, Tuple2<String, String>... columnNameAndValues) {
        StringBuilder selectSql = new StringBuilder("select * from " + GmallConfig.PHOENIX_SCHEMA + "." + tableName + " where ");

        for (int i = 0; i < columnNameAndValues.length; i++) {
            Tuple2<String, String> columnNameAndValue = columnNameAndValues[i];
            String columnName = columnNameAndValue.f0;
            String columnValue = columnNameAndValue.f1;
            selectSql.append(columnName + "='" + columnValue + "' ");
            if (i < columnNameAndValues.length - 1) {
                selectSql.append(" and ");
            }
        }
        System.out.println("从phoenix表中查询数据的sql:" + selectSql);

        //到phoenix表中查询维度数据  底层还是调用PhoenixUtil
        List<JSONObject> jsonObjList = PhoenixUtil.queryList(conn, selectSql.toString(), JSONObject.class);
        //虽然调用PhoenixUtil工具类的查询方法返回的是List集合，但是我们在查询维度的时候，加了主键查询条件，所以如果维度存在，我们只会查询到一条结果
        JSONObject dimJsonObj = null;
        if (jsonObjList != null && jsonObjList.size() > 0) {
            dimJsonObj = jsonObjList.get(0);
        } else {
            System.out.println("在phoenix表中，没有找到对应的维度数据:" + selectSql);
        }
        return dimJsonObj;
    }

    public static void deleteCached(String tableName,String id){
        String redisKey = "dim:" + tableName.toLowerCase() + ":" + id;
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getJedis();
            jedis.del(redisKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("清除Redis中缓存的维度数据失败~~~");
        } finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        DruidDataSource dataSource = DruidDSUtil.createDataSource();
        DruidPooledConnection conn = dataSource.getConnection();
        //System.out.println(getDimInfoNoCache(conn, "DIM_BASE_TRADEMARK", Tuple2.of("ID", "5")));
        //System.out.println(getDimInfo(conn, "DIM_BASE_TRADEMARK", Tuple2.of("ID", "5")));
        System.out.println(getDimInfo(conn, "DIM_BASE_TRADEMARK", "6"));
    }
}
