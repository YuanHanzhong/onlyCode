package _30_JDBC.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
1、什么是JDBC
JDBC是Java程序连接各种数据库的API。
它由一组公共的接口（由SUN/Oracle提供） + 各个数据库厂商提供的驱动类构成。

2、如何使用JDBC？
第一步：先下载驱动，并且把它引入到咱们的项目中的classpath下
A：把jar包放到项目的jdbclibs文件夹中
B：项目设置中，libraries->+->Java ->jdbclibs文件夹-->应用到模块

第二步：编写代码连接mysql数据库
Class.forName("com.mysql.cj.jdbc.Driver");
String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
Connection conn = DriverManager.getConnection(url, "root", "root");
 */
public class TestJdbc {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
       Class.forName("com.mysql.cj.jdbc.Driver");//如果web阶段学习时，遇到在tomcat中找不到驱动类，可以加这句代码手动加载驱动类。

        //DriverManager：驱动管理
        //getConnection：获取连接
        /*
        A：提供mysql服务所在主机的IP地址或主机名
        B：端口号
        C：用户名
        D：密码
        E：要连接的数据库名称（这一点和使用命令行客户端、可视化工具客户端不一样），一个连接只能查看一个数据库

        如何把这些信息告诉底层呢？
        通过url  协议://主机名:端口号/数据库名?参数名=参数值
        http://www.atguigu.com
        jdbc:mysql://localhost:3306/atguigu
        jdbc是主协议，mysql是子协议，localhost表示连接本机的mysql数据库，3306是端口号，atguigu是一个数据库名
         */
        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        /*
        如果url后面不加 ?serverTimezone=UTC，会报异常：
        Exception in thread "main" java.sql.SQLException: The server time zone value '�й���׼ʱ��' is unrecognized or represents more than one time zone. You must configure either the server or JDBC driver (via the 'serverTimezone' configuration property) to use a more specifc time zone value if you want to utilize time zone support.

         */
        Connection conn = DriverManager.getConnection(url, "root", "root");
        System.out.println(conn);
    }
}
