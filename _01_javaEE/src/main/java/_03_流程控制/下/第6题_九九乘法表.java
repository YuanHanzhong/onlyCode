package _03_流程控制.下;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class 第6题_九九乘法表 {
    public static void main(String[] args) {
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j <=i; j++) {
                System.out.print(j+"*"+i +"="+i*j+"\t");
            }
            System.out.println();
        }
    }
}
