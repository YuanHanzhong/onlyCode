package _30_JDBC.problem;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/*
5、问题5：批处理

例如：演示批量的添加模拟数据到部门表（选择部门表是因为字段少，员工字段太多）。
添加1000条记录

批处理的方法：
（1）每次设置完？之后，先不执行，先攒着
pst.addBatch(); 把要执行sql先添加到批处理队伍中，在缓存中攒着
（2）等所有的设置完了之后，一口气执行

其实如果缓冲区满的时候，它也会自动执行一次。
如果缓冲区没有溢出，那么会最后一起执行

批处理要注意，
(1)mysql默认批处理功能没开启，需要通过参数告知服务器端开启的批处理。
在url中需要加一个参数  rewriteBatchedStatements=true

jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC&rewriteBatchedStatements=true

(2)insert语句不要用value，要用values
 */
public class Problem5 {
    @Test
    public void test03()throws Exception{
        long start = System.currentTimeMillis();

        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC&rewriteBatchedStatements=true";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "INSERT INTO t_department VALUE(null,?,?)";
        //如果此处不小心把values写成value
        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

        //设置？的值
        for(int i=1; i<=1000; i++){
            pst.setObject(1, "测试部门名称"+i);
            pst.setObject(2, "测试部门简介"+i);

            pst.addBatch();
        }
        pst.executeBatch();//执行批处理命令

        pst.close();
        conn.close();

        long end  = System.currentTimeMillis();
        System.out.println("耗时："  + (end-start));//耗时：4362
    }

    @Test
    public void test02()throws Exception{
        long start = System.currentTimeMillis();

        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC&rewriteBatchedStatements=true";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "INSERT INTO t_department VALUES(null,?,?)";
        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

        //设置？的值
        for(int i=1; i<=1000; i++){
            pst.setObject(1, "测试部门名称"+i);
            pst.setObject(2, "测试部门简介"+i);

            pst.addBatch();
        }
        pst.executeBatch();//执行批处理命令

        pst.close();
        conn.close();

        long end  = System.currentTimeMillis();
        System.out.println("耗时："  + (end-start));//耗时：1242
    }

    @Test
    public void test01()throws Exception{
        long start = System.currentTimeMillis();

        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "INSERT INTO t_department VALUES(null,?,?)";
        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

        //设置？的值
        for(int i=1; i<=1000; i++){
            pst.setObject(1, "测试部门名称"+i+"11111");
            pst.setObject(2, "测试部门简介"+i+"11111");

            pst.executeUpdate();//每设置一次，执行一次
        }

        pst.close();
        conn.close();

        long end  = System.currentTimeMillis();
        System.out.println("耗时："  + (end-start));//耗时：4324
    }
}
