package _07_面向对象;/*
 *需求: 用来测试别的类
 *要点:
 *      1
 *      2
 *      3
 */

import org.junit.Test;

public class 入口 {
    public static void main(String[] args) {
        Employee a = new Employee(); // 这样创建对象. 类名是要出现2次的 got
        Employee employee = new Employee();
        a.age = 14;
        a.id = 1;
        a.name = "Jack";
        a.salary = 300000;
    
        employee.name = "jack";
    
        System.out.println(employee.name);
    
        System.out.println(a.age);
        
        
    }
    
    @Test
    public void test() {
        /**
         * 题目：测试日期类
         * 要点：
         */
        // got 一说对象就是先想到new
    
        Riqi riqi = new Riqi();
        riqi.day = 2;
        riqi.month = 2;
        riqi.year = 2;
    
        Riqi riqi1 = new Riqi();
        riqi1.day = 3;
    
        System.out.println(riqi1.day);
        System.out.println(riqi.month);
    
    
    }
    
    @Test
    public void test4() {
        /**
         * 题目：在测试类的main方法中创建三角形对象，将三角形的三条边设置为3,4,5，调用方法测试。
         * 要点：
         */
    
        Sanjiao sanjiao = new Sanjiao();
        sanjiao.b =3;
        sanjiao.a =4;
        sanjiao.c=5;
    
        if (sanjiao.isRightTriangle()) {
            System.out.println("是三角");
        }
    
        if (sanjiao.isRightTriangle()) {
            System.out.println("直j");
        }
    
    }
    
    @Test
    public void test5() {
        /**
         * 题目：在测试类的main方法中，创建MyDate对象，赋值为当天日期值，调用方法测试。
         * 要点：
         */
        Riqi riqi = new Riqi();
        riqi.year = 300;
        
        if (riqi.isLeapYear()) {
    
            System.out.println("run");
        }
    }
    
    @Test
    public void test6() {
        /**
         * 题目：
         * 要点：
         */
        
        int[] a={1, 3, 4, 3, 1, 5};
        ArrayTools arrayTools = new ArrayTools();
        int i = arrayTools.indexOf(a, 3);
        System.out.println(i);
    
        System.out.println("arrayTools.lastIndexOf(a,3) = " + arrayTools.lastIndexOf(a, 3));
    
    }
    
    @Test
    public void test7() {
        /**
         * 题目：
         * 要点：
         */
        int[] a = {2, 3, 4, 5};
        
        
        for (int i : a) { // i不是下标, 而是内容 got
            System.out.println("i = " + i);
            System.out.println("a[i] = " + a[i-2]);
        }
    }
}
