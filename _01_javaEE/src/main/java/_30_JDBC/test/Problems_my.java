package _30_JDBC.test;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import org.junit.Test;

import java.util.Scanner;

public class Problems_my {
    @Test
    public void test() {
        /**
         * 题目：
         * 要点：
         */
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
    }
}
