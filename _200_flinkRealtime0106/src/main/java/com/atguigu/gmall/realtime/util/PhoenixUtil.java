package com.atguigu.gmall.realtime.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.beanutils.BeanUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Felix
 * Date: 2022/7/1
 * Desc: 操作Phoenix的工具类
 */
public class PhoenixUtil {
    public static void executeSql(String sql, Connection conn) {
        PreparedStatement ps = null;
        try {
            //创建数据库操作对象
            ps = conn.prepareStatement(sql.toString());
            //执行sql语句
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("~~执行Phoenix语句失败:" + sql);
        } finally {
            //释放资源
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从phoenix表中查询数据2
     * @param conn 连接对象
     * @param sql  执行的查询语句
     * @param clz  将查询结果封装的类型
     * @return 多条查询结果
     */
    public static <T> List<T> queryList(Connection conn, String sql, Class<T> clz) {

        List<T> resList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            //创建数据库操作对象
            ps = conn.prepareStatement(sql);
            //执行SQL语句
            rs = ps.executeQuery();
          /*
            +-----+------------+
            | ID  |  TM_NAME   |
            +-----+------------+
            | 1   | 三星         |
            | 10  | 欧莱雅        |
            */
            ResultSetMetaData metaData = rs.getMetaData();
            //处理结果集
            while (rs.next()) {
                //每遍历一次，相当于获取一条查询结果，我们要将查询结果赋值给对象
                //创建要封装的类型对象
                T obj = clz.newInstance();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = rs.getObject(i);
                    BeanUtils.setProperty(obj, columnName, columnValue);
                }
                resList.add(obj);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("~~从phoenix表中查询维度数据发生了异常~~");
        } finally {
            //释放资源
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return resList;
    }

    public static void main(String[] args) throws SQLException {
        DruidDataSource dataSource = DruidDSUtil.createDataSource();
        DruidPooledConnection conn = dataSource.getConnection();
        List<JSONObject> list = queryList(conn, "select * from GMALL2022_REALTIME.DIM_BASE_TRADEMARK", JSONObject.class);
        System.out.println(list);
    }
}
