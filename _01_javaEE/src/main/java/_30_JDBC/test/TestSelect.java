package _30_JDBC.test;


/*
6、JDBC实现查询数据
（1）连接数据库获取Connection对象   ==>Socket
（2）编写select的sql
SELECT * FROM t_department;
（3）获取一个PreparedStatement对象  ==> 输出流/输入流
（4）执行sql
只要是修改数据库的数据（添加、删除、修改），调用的方法都是 executeUpdate()，它的返回值类型int
执行查询数据库的数据，调用的方法是executeQuery()，它的返回值类型ResultSet结果集

（5）遍历结果集
while(rs.next()){
    变量 = rs.getXxx(字段名或字段序号)
}
（6）释放资源
 */

import java.sql.*;

public class TestSelect {
    public static void main(String[] args)throws SQLException {
        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "SELECT * FROM t_department";
        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

        ResultSet rs = pst.executeQuery();//返回一个结果集
        //遍历结果集
/*        while(rs.next()){//相当于Collection系列集合得到的Iterator迭代器的hasNext()方法
            int did = rs.getInt("did");//数据中表的("字段名")
            String dname = rs.getString("dname");
            String desc = rs.getString("description");
            System.out.println(did+"\t" +dname +"\t" + desc);
        }*/

        while(rs.next()){//相当于Collection系列集合得到的Iterator迭代器的hasNext()方法
            Object did = rs.getObject("did");//数据中表的字段的序号，第1列
            Object dname = rs.getObject("dname");//数据中表的字段的序号，第2列
            Object desc = rs.getObject("description");//数据中表的字段的序号，第3列
            System.out.println(did+"\t" +dname +"\t" + desc);
        }

        rs.close();
        pst.close();
        conn.close();
    }
}
