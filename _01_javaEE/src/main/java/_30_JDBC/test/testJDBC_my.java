package _30_JDBC.test;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class testJDBC_my {
    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC", "root", "root");
        System.out.println("connection = " + connection);
        connection.close();
    }

}
