package _30_JDBC.pool;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/*
使用阿里的Druid的步骤：
（1）先找到jar包，放到项目的jdbclibs文件中，重新添加libriaries中
（2）编写配置文件
    src下加一个druid.properties文件
    或
    在模块根目录下，再建一个文件夹config，把config文件设置为源代码文件夹，Mark directory as  Source Root。
    这样做的 目的是把配置文件和.java文件分开放，编译后在一起。
    在config文件夹下面建一个druid.properties文件

 文件里面的内容是：

 #key=value
driverClassName=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC&rewriteBatchedStatements=true
username=root
password=root
initialSize=5   初始化连接池
maxActive=10    最多连接池
maxWait=1000    等待时间，如果超过1000毫秒还没有分配到连接，就报异常

数据库连接池的目的是管理连接，需要与数据库建立连接。
需要指定驱动、url、用户名、密码等。

（3）从数据库连接池中获取连接
通过德鲁伊的数据库连接的工厂类创建数据库连接池，再从池中获取连接
 */
public class TestDruid {
    static int i;
    public static void main(String[] args) throws Exception{
        //加载配置文件
        Properties properties = new Properties();
        //TestDruid.class.getClassLoader()获取当前类的类加载器对象
        properties.load(TestDruid.class.getClassLoader().getResourceAsStream("druid.properties"));
        DataSource ds = DruidDataSourceFactory.createDataSource(properties);//创建数据库连接池
    /*    Connection conn = ds.getConnection();//获取一个连接对象
        System.out.println(conn);*/

        for(i=1; i<=15; i++) {
            new Thread() {
                public void run() {
                    try {
                        Connection conn = ds.getConnection();//获取一个连接对象
                        System.out.println("第" + i + "个连接：" + conn);

                        try {
                            Thread.sleep(100);//故意延迟时间
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        conn.close();//这里不是彻底断开，还给连接池
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }


        /*Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");
        System.out.println(conn);*/
    }
}
