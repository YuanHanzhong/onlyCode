package _04_数组.二维数组;/*
 *需求:
1
1 1
1 2 1
1 3 3  1
1 4 6  4  1
1 5 10 10 5 1

 *要点:
 *      1 找规律而已
 *      2
 *      3
 */

public class 杨辉三角 {
    public static void main(String[] args) {
        int[][] ints = new int[10][10];
        
        // 预赋值
        for (int i = 0; i < ints.length; i++) {
            ints[i][0] = 1;
            ints[i][i] = 1;
        }
        
        // 收尾赋值
        for (int i = 2; i < ints.length; i++) {
            for (int j = 1; j < i; j++) {
                ints[i][j] = ints[i - 1][j] + ints[i - 1][j - 1];
            }
        }
    
        // 显示
        for (int i = 0; i < ints.length; i++) {
         
            for (int j = 0; j <= i; j++) {
                System.out.print(ints[i][j]+ "\t");
            }
            System.out.println();
        }
    }
}
