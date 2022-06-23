package _30_JDBC.problem;

import org.junit.Test;

import java.sql.*;
import java.util.Date;
import java.util.Scanner;

/*
4、自增长键值的问题
希望自增长的字段，在添加完成后，可以及时的反馈给Java程序端。
解决方案：
（1）PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
这里的Statement是一个接口，它是PreparedStatement的父接口。
Statement接口中有一个常量值 RETURN_GENERATED_KEYS，表示执行Sql的同时，返回自增长的键值

（2） ResultSet rs = pst.getGeneratedKeys();//返回的是一个结果集
 */
public class Problem4 {
    @Test
    public void test01()throws Exception{
        Scanner input = new Scanner(System.in);
        System.out.print("请输入姓名：");
        String ename = input.next();//李四

        System.out.print("请输入薪资：");
        double salary = input.nextDouble();//15000

        System.out.print("请输入出生日期：");
        String birthday = input.next();//1990-1-1

        System.out.print("请输入性别：");
//        char gender = input.next().charAt(0);//男
        String gender = input.next();//男

        System.out.print("请输入手机号码：");
        String tel = input.next();//13578595685

        System.out.print("请输入邮箱：");
        String email = input.next();//zhangsan@atguigu.com

        input.close();

        //把上面的数据添加到数据库中atguigu库的t_employee
        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "INSERT INTO t_employee(ename,salary,birthday,gender,tel,email,hiredate)VALUES(?,?,?,?,?,?,?)";
        PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);//相当于通过PreparedStatement对象把sql发送给服务器端

        /*
        多加一步，把?用具体的变量、表达式等值代替

         */
        pst.setObject(1,ename);//这里1代表是第1个?，用ename变量的值来代替第一个?的位置
        pst.setObject(2,salary);
        pst.setObject(3,birthday);
        pst.setObject(4,gender);
        pst.setObject(5,tel);
        pst.setObject(6,email);
        pst.setObject(7,new Date());

        int len = pst.executeUpdate(); //len代表的是影响的记录数
        System.out.println(len>0 ? "添加成功" : "添加失败");

        //执行完之后，通过PreparedStatement对象pst获取自增长键值
        ResultSet rs = pst.getGeneratedKeys();//返回的是一个结果集
        while(rs.next()) {//先判断是否有记录，next()会移动游标，这一点和集合不同
            System.out.println("您的员工编号是：" + rs.getObject(1));//此处只有一个自增长的键值
        }
        pst.close();
        conn.close();

    }
}
