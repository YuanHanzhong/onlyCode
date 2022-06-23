package _02_基础语法拓展;/*
 *需求: // 拆分任意
 *要点:
 *      1
 *      2
 *      3
 */

public class _7_获取一个四位数的个位十位百位千位 {
    public static void main(String[] args) {
        int a = 4328;
        
        int qian = a/1000;
        int bai = a%1000/100;
        int shi = a%100/10;
        int ge = a%10;
    
        System.out.println("qian = " + qian);
        System.out.println("bai = " + bai);
        System.out.println("shi = " + shi);
        System.out.println("ge = " + ge);
    
    }
}
