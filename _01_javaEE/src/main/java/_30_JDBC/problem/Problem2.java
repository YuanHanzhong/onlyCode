package _30_JDBC.problem;

import org.junit.Test;

import java.sql.*;
import java.util.Scanner;

/*
2、问题2：sql注入问题
 */
public class Problem2 {
    @Test
    public void test02()throws Exception{
        Scanner input = new Scanner(System.in);
        System.out.print("请输入你要查询的员工的编号：");
        String id = input.nextLine();/*
        此时我输入的数据是正常员工编号，下面的代码没问题
         此时我输入的数据是 1 or 1=1
        */
        input.close();

        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "SELECT * FROM t_employee WHERE eid = ?";

        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

        //设置?的值
        pst.setObject(1,id);

        ResultSet rs = pst.executeQuery();//返回一个结果集
        //遍历结果集
        //结果集中有一个 元数据对象
        //元数据：描述数据的数据，对数据进行描述的信息，例如：数据列有多少列，数据的列名称等
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();//可以获取结果集中有几列

        while(rs.next()){//相当于Collection系列集合得到的Iterator迭代器的hasNext()方法
            //while循环一次，是循环一行
            for(int i=1; i<=columnCount; i++){//for循环循环一次是访问一行中的一列，一个单元格
                System.out.print(rs.getObject(i)+"\t");//列的序号是从1开始
            }
            System.out.println();
        }

        rs.close();
        pst.close();
        conn.close();
    }

    @Test
    public void test01()throws Exception{
        Scanner input = new Scanner(System.in);
        System.out.print("请输入你要查询的员工的编号：");
        String id = input.nextLine();/*
        此时我输入的数据是正常员工编号，下面的代码没问题
         此时我输入的数据是 1 or 1=1
        */
        input.close();

        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "SELECT * FROM t_employee WHERE eid = " + id;
        /*
        SELECT * FROM t_employee WHERE eid = 1;
        SELECT * FROM t_employee WHERE eid = 1 or 1=1;  1=1永远成立，表示其他的条件都失效了
         */

        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

        ResultSet rs = pst.executeQuery();//返回一个结果集
        //遍历结果集
        //结果集中有一个 元数据对象
        //元数据：描述数据的数据，对数据进行描述的信息，例如：数据列有多少列，数据的列名称等
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();//可以获取结果集中有几列

        while(rs.next()){//相当于Collection系列集合得到的Iterator迭代器的hasNext()方法
            //while循环一次，是循环一行
            for(int i=1; i<=columnCount; i++){//for循环循环一次是访问一行中的一列，一个单元格
                System.out.print(rs.getObject(i)+"\t");//列的序号是从1开始
            }
            System.out.println();
        }

        rs.close();
        pst.close();
        conn.close();
    }
}
