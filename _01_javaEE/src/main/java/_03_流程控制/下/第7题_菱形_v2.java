package _03_流程控制.下;/*
 *需求:
 *要点:
 *      1 应该是先分析, 尽量分析清楚, 不是上来就敲
 *      2 * 1 3 5 7
 *      3 空白 7 5 3 1
 间距是2 , 所以最好的是++++, 而不是通过乘以, i = i+2;
 */

public class 第7题_菱形_v2 {
    public static void main(String[] args) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j <6*2 - i*2; j++) {
                System.out.print(" ");
            }
            for (int j = 0; j < i*2 -1; j++) {
                System.out.print("* ");
            }
            System.out.println();
        }
    
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j <i*2; j++) {
                System.out.print(" ");
            }
            for (int j = 0; j < 6*2 - i*2 -1; j++) {
                System.out.print("* ");
            }
            System.out.println();
        }
    }
}
