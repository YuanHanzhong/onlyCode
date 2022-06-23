package _30_JDBC.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
4、JDBC实现修改数据
（1）连接数据库获取Connection对象   ==>Socket
（2）编写update的sql
（3）获取一个PreparedStatement对象  ==> 输出流/输入流
（4）执行sql
只要是修改数据库的数据（添加、删除、修改），调用的方法都是 executeUpdate()
（5）释放资源
 */
public class TestUpdate {
    public static void main(String[] args)throws SQLException {
        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "UPDATE t_department SET description = '发很多很多的钱' WHERE did = 7";
        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

        int len = pst.executeUpdate(); //len代表的是影响的记录数
        System.out.println(len>0 ? "修改成功" : "修改失败");

        pst.close();
        conn.close();
    }
}
