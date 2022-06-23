package _30_JDBC.problem;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.Scanner;

/*
问题1：sql的拼接问题
 */
public class Problem1 {
    
    @Test
    public void test02()throws Exception{
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
        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

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

        pst.close();
        conn.close();

    }

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
        char gender = input.next().charAt(0);//男

        System.out.print("请输入手机号码：");
        String tel = input.next();//13578595685

        System.out.print("请输入邮箱：");
        String email = input.next();//zhangsan@atguigu.com

        input.close();

        //把上面的数据添加到数据库中atguigu库的t_employee
        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "INSERT INTO t_employee(ename,salary,birthday,gender,tel,email,hiredate)VALUES('" + ename +"'," + salary+",'"+birthday+"','"+gender+"','"+tel+"','"+email+"',curdate())";
        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

        int len = pst.executeUpdate(); //len代表的是影响的记录数
        System.out.println(len>0 ? "添加成功" : "添加失败");

        pst.close();
        conn.close();

    }
}
