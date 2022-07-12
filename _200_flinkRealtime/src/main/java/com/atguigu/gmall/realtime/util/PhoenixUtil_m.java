package com.atguigu.gmall.realtime.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.beanutils.BeanUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Author: Felix
 * Date: 2022/5/20
 * Desc: 操作phoenix的工具类
 */
public class PhoenixUtil_m {
    //执行DDL以及DML
    public static void executeSQL(String sql, Connection conn) {
        PreparedStatement ps = null;
        try {
            //获取数据库操作对象
            ps = conn.prepareStatement(sql);
            //执行SQL语句
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("执行操作phoenix语句发生了异常");
        }finally {
            close(ps,conn);
        }
    }
    public static void close(PreparedStatement ps, Connection conn){
        if(ps != null){
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        从Phoenix表中查询数据
        +-----+------------+
        | ID  |  TM_NAME   |
        +-----+------------+
        | 1   | 三星         |
        | 10  | 欧莱雅        |
        | 11  | 香奈儿        |
    */
    public static <T>List<T> queryList(Connection conn,String sql,Class<T> clz){
        List<T> resList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //获取数据库操作对象
            ps = conn.prepareStatement(sql);
            //执行sql语句
            rs = ps.executeQuery();
            //获取查询结果的元数据信息
            ResultSetMetaData metaData = rs.getMetaData();
            //处理结果集
            while (rs.next()){
                //创建一个对象 用于接收遍历出来的一条结果集
                T obj = clz.newInstance();
                //获取表中字段的名称
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = rs.getObject(i);
                    //给对象的属性赋值
                    BeanUtils.setProperty(obj,columnName,columnValue);
                }
                resList.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //释放资源
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(conn != null){
                try {
                    conn.close();
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
        System.out.println(queryList(conn, "select * from gmall2022_REALTIME.dim_base_trademark", JSONObject.class));
    }
}
