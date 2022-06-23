package _03_流程控制.上;/*
 *需求:
         8、求ax (2)+bx+c=0方程的根，其中a,b,c分别为函数的参数。
            如果a≠0，那么：
            （1）当b (2)-4ac>0，则一元二次方程有两个实数解：
            （2）当b (2)-4ac=0，则一元二次方程有一个实数解：
            （3）当b (2)-4ac<0，则一元二次方程在实数范围内无解；
        如果a=0,b≠0，那么一元一次方程有一个解：
        如果a=0,b=0，那么参数输入有误，该式子不是方程。
        提示1：Math.sqrt(num);  sqrt指平方根
 *要点:
 *      1 不管是new 还是基本数据类型, 声明时就给初始值比较好.
 *      2
 *      3
 */

import java.util.Scanner;

public class 第8题_解方程 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("a: ");
        double a = scanner.nextDouble();
        System.out.println("b: ");
    
        double b = scanner.nextDouble();
        System.out.println("c: ");
    
        double c = scanner.nextDouble();
        
        scanner.close(); // got 用完就关, 好习惯
        //double x1=0; // got 初始化为多少比较好. 都不好, 就是用时再double
        //double x2=0;
        
        // got 注意
        double v2 = b*b - 4*a*c;
        //double v = Math.sqrt(v2) / (2 * a); // star 这里翻译错了, 提前除了
        double v = Math.sqrt(v2) ; // star 这里翻译错了, 少写了-b
        
        if (a != 0) {
            if (v2 > 0) {
                double x1 = (-b + v)/(2*a);
                System.out.println("v = " + v);
                System.out.println("v2 = " + v2);
                System.out.println("x1 = " + x1);
                double x2 = (-b - v)/(2*a);
                System.out.println("x2 = " + x2);
                System.out.println("v = " + v);
                System.out.println("v2 = " + v2);
       
                System.out.println("x1 = " + x1);
                System.out.println("x2 = " + x2);
            } else if (0 == v2) { // got 判断相等时, 把实数写在前面
                double x1 = -(b/(2*a));
                System.out.println("方程只有1个根 " + x1);
                System.out.println("v = " + v);
                System.out.println("v2 = " + v2);
            } else if (0 > v2) {
                System.out.println("wujie");
                System.out.println("v = " + v);
                System.out.println("v2 = " + v2);
            }
        } else if (0 == a && 0 != b) {
            double x1 = -c/b;
            System.out.println("方程只有1个根 :" + x1);
            
        } else if (0 == a && 0 == b) {
            System.out.println("参数有误");
        }

    }
}
