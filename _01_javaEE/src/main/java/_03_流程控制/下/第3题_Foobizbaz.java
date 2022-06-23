package _03_流程控制.下;/*
 *需求: 3、从1循环到150并在每行打印一个值，另外在每个3的倍数行上打印出“foo”,在每个5的倍数行上打印“biz”,在每个7的倍数行上打印输出“baz”。例如
 *要点:
 *      1
 *      2
 *      3
 */

public class 第3题_Foobizbaz {
    public static void main(String[] args) {
        for (int i = 1; i < 151; i++) {
            System.out.print(i);
            
            if (0 == i % 3) {
                System.out.print("\tfoo");
            }
    
            if (0 == i % 5) {
                System.out.print("\tbiz");
            }
    
            if (0 == i % 7) {
                System.out.print("\tbaz");
            }
            System.out.println();
        }
    }
    
}
