package _30_JDBC.problem;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
6、事务处理问题

例子：
    修改部门表中的 两条记录，故意分开用两个update语句来修改。
    然后希望这两条update语句，构成一个事务，要么一起成功，然后提交，要么一起失败，还原回滚。

    update t_department set description = 'xx' where did = 2;
	update t_department set description = 'yy' where did = 3;

JDBC中提交事务的方法：
 Connection连接对象.commit();
JDBC中回滚事务的方法：
 Connection连接对象.rollback();

注意：mysql端事务默认是自动提交的，需要开启手动提交模式，才能手动提交事务。
Connection连接对象.setAutoCommit(false)  手动提交
Connection连接对象.setAutoCommit(true)  自动提交

 */
public class Problem6 {
    @Test
    public void test01()throws Exception{
        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        conn.setAutoCommit(false);//手动提交模式

        String sql1 = "update t_department set description = 'xx' where did = 2";
        String sql2 = "update t_department set description = 'yy' what did = 3";//故意把第二条语句写错，where写错成what

        PreparedStatement pst1 = conn.prepareStatement(sql1);
        PreparedStatement pst2 = conn.prepareStatement(sql2);

        try {
            pst1.executeUpdate(); //len代表的是影响的记录数
            pst2.executeUpdate(); //len代表的是影响的记录数

            //没有异常就是成功
            System.out.println("两条都成功");
            conn.commit();//提交事务
        } catch (SQLException e) {
            System.out.println("有失败的");
            conn.rollback();//回滚
        }

        pst1.close();
        pst2.close();

        //这里习惯上，在close之前，会把连接重新设置为 自动提交模式
        conn.setAutoCommit(true);//这个在本例中没有意义，但是我们接下来马上要学习数据库连接池，连接会被重复使用，在关闭（还给连接池时）需要恢复自动提交模式
        conn.close();
    }

}
