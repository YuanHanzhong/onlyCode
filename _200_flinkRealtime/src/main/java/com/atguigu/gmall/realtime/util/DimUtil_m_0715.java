package com.atguigu.gmall.realtime.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.realtime.common.GmallConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/*
 * Desc: 维度查询工具类
 */
public class DimUtil_m_0715 {
    
    // 2022/7/16 10:18 NOTE 进一步优化, 只传id就行
    public static JSONObject getDimInfo(Connection conn, String tableName, String id) {
        return getDimInfo(conn, tableName, Tuple2.of("ID", id));
    }
    
    
    /*
     查询维度信息
     优化：旁路缓存
     思路：
          先从缓存中查询维度信息，如果缓存中存在，直接将查询到的维度信息作为返回值返回(缓存命中)
          如果缓存中不存在查找的维度，再发送请求到phoenix表中查询，并将查询的维度放到缓存中缓存起来
          
     选型：
          状态
              性能好、维度性差
          Redis   √
              性能也说得过去、方便维护
     Redis分析：
          类型：String
          TTL: 1day
          key:    dim:表名:主键1_主键2
          注意：如果业务数据库维度表发生了变化，清除缓存
     */
    
    // 2022/7/15 15:53  多个查询条件优化
    // 使用旁路缓存 P2
    public static JSONObject getDimInfo(Connection conn, String tableName, Tuple2<String, String>... columnNameAndValues) {
        //初始拼接查询Redis的key
        StringBuilder redisKey = new StringBuilder("dim:" + tableName.toLowerCase() + ":"); // 2022/7/16 9:28 NOTE 拼接用stringBuilder
        
        //初始拼接查询语句
        StringBuilder selectSql = new StringBuilder("select * from " + GmallConfig.PHOENIX_SCHEMA + "." + tableName + " where ");
        
        // 完成拼接
        for (int i = 0; i < columnNameAndValues.length; i++) {
            Tuple2<String, String> columnNameAndValue = columnNameAndValues[i];
            
            String columnName = columnNameAndValue.f0;
            String columnValue = columnNameAndValue.f1;
            
            selectSql.append(columnName + " = '" + columnValue + "'");
            redisKey.append(columnValue);
            if (i < columnNameAndValues.length - 1) {
                selectSql.append(" and ");
                redisKey.append("_");
            }
        }
        
        //从Redis中查询维度数据
        Jedis jedis = null; // 获取jedis连接
        String dimInfoStr = null; // redis中的信息
        JSONObject dimJsonObj = null; //  phoenix中的信息
        
        try {
            jedis = RedisUtil.getJedis(); // 获取客户端
            dimInfoStr = jedis.get(redisKey.toString());
        } catch (Exception e) {
            e.printStackTrace(); // 2022/7/16 13:56 NOTE 对异常的处理 选上 , 右键, 异常
            System.out.println("从Redis中查询维度发生了异常~~");
        }
    
        //Redis中查询到了维度，缓存命中
        if (StringUtils.isNotEmpty(dimInfoStr)) {
            dimJsonObj = JSONObject.parseObject(dimInfoStr);
            //NOTE 这里不能return, 会导致关闭不了, 导致资源越来越少
        } else {
            
            //如果在缓存中没有查询到维度，那么发送请求到phoenix表中进行查询
            System.out.println("从Phoenix表中查询维度的SQL:" + selectSql);
            //底层还是到Phoenix表中查询数据，所以直接调用PhoenixUtil工具类中的查询方法即可
            List<JSONObject> jsonObjList = PhoenixUtil.queryList(conn, selectSql.toString(), JSONObject.class);
            //注意：虽然方法的返回值是list类型，但是我们在查询维度的时候加了查询条件，所以维度不会返回多条，如果有，只要有一条结果
            if (jsonObjList.size() > 0) {
                dimJsonObj = jsonObjList.get(0);
                //将查询到的数据缓存到Redis中
                if (jedis != null) {
                    jedis.setex(redisKey.toString(), 3600 * 24, dimJsonObj.toJSONString());
                }
            } else {
                System.out.println("从Phoenix维度表中没有查到对应的维度:" + selectSql);
            }
        }
        
        //释放资源, NOTE new了就先写close, 防止忘
        if (jedis != null) {
            System.out.println("~~~关闭Jedis客户端~~~");
            jedis.close();
        }
        return dimJsonObj;
    }
    
    /**
     * 查询维度数据, 未优化
     *
     * @param conn
     *   连接对象
     * @param tableName
     *   表名
     * @param columnNameAndValues
     *   查询条件
     *
     * @return
     */
    public static JSONObject getDimInfoNoCache(Connection conn, String tableName, Tuple2<String, String>... columnNameAndValues) {
        //拼接查询语句
        StringBuilder selectSql = new StringBuilder("select * from " + GmallConfig.PHOENIX_SCHEMA + "." + tableName + " where ");
        
        // NOTE 对字符串的操作, 拼接成SQL,
        for (int i = 0; i < columnNameAndValues.length; i++) {
            
            Tuple2<String, String> columnNameAndValue = columnNameAndValues[i];
            String columnName = columnNameAndValue.f0;
            String columnValue = columnNameAndValue.f1;
            
            selectSql.append(columnName + " = '" + columnValue + "'");
            if (i < columnNameAndValues.length - 1) {
                selectSql.append(" and ");
            }
        }
        
        System.out.println("从Phoenix表中查询维度的SQL:" + selectSql);
        
        //底层还是到Phoenix表中查询数据，所以直接调用PhoenixUtil工具类中的查询方法即可
        List<JSONObject> jsonObjList = PhoenixUtil.queryList(conn, selectSql.toString(), JSONObject.class);
        //注意：虽然方法的返回值是list类型，但是我们在查询维度的时候加了查询条件，所以维度不会返回多条，如果有，只要有一条结果
        JSONObject dimJsonObj = null;
        
        if (jsonObjList.size() > 0) {
            dimJsonObj = jsonObjList.get(0);
        } else {
            System.out.println("从Phoenix维度表中没有查到对应的维度:" + selectSql);
        }
        
        return dimJsonObj;
    }
    
    // 在upsert到phonix时调用 DimSinkApp
    public static void deleteCached(String tableName, String id) {
        // 2022/7/16 10:23 NOTE 一步步来, 先只考虑最简单情况, 先打通. 再慢慢填充细节, 优化.
        String redisKey = "dim:" + tableName.toLowerCase() + ":" + id;
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getJedis();
            jedis.del(redisKey);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("从Redis中删除缓存的数据失败~~~");
        } finally {
            if (jedis != null) { // 2022/7/16 10:23 NOTE . 之前, 判断非空
                jedis.close();
            }
        }
    }
    
    public static void main(String[] args) throws SQLException {
        DruidDataSource dataSource = DruidDSUtil.createDataSource();
        DruidPooledConnection conn = dataSource.getConnection();
        //JSONObject jsonObj = getDimInfoNoCache(conn, "dim_base_trademark", Tuple2.of("id", "1"), Tuple2.of("tm_name", "三星"));
        JSONObject jsonObj = getDimInfo(conn, "dim_base_trademark", Tuple2.of("id","1"), Tuple2.of("tm_name","mike"));
        JSONObject jsonObj2 = getDimInfo(conn, "dim_base_trademark", "2"); //测试只传ID,
        System.out.println(jsonObj);
        System.out.println(jsonObj2);
    }
}
