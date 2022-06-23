package _30_JDBC.problem;

import org.junit.Test;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;

/*
3、问题3：处理blob类型的数据
A：数据类型本身有大小限制
每一种blob都有大小的范围
B：客户端与服务器端的packet有大小限制
max_allowed_packet=16M
这个参数默认大小是4M

 */
public class Problem3 {
    @Test
    public void test01()throws Exception{
        Scanner input = new Scanner(System.in);
        System.out.print("请输入用户名：");
        String username = input.next();

        System.out.print("请输入密码：");
        String password = input.next();

        System.out.print("请选择照片：");
        String path = input.next();//这里没有图形化界面，只能输入路径，通过IO流读取图片的内容

        input.close();

        String url = "jdbc:mysql://localhost:3306/atguigu?serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "root");

        String sql = "INSERT INTO t_user VALUES(?,md5(?),?)";
        PreparedStatement pst = conn.prepareStatement(sql);//相当于通过PreparedStatement对象把sql发送给服务器端

        //设置？的值
        pst.setObject(1,username);
        pst.setObject(2,password);
//        pst.setObject(3,path);//这样只是存储了图片的路径
        pst.setObject(3, new FileInputStream(path));//文件字节IO流中包含文件内容

        int len = pst.executeUpdate(); //len代表的是影响的记录数
        System.out.println(len>0 ? "添加成功" : "添加失败");

        pst.close();
        conn.close();
    }
}
