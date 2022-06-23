package _03_流程控制.下;/*
 *需求:
 *要点:
         // star 步步为营, 上面找到规律后, 下面的是复制出来的

 */

public class 第7题_菱形 {
    public static void main(String[] args) {
        for (int i = 0; i < 16; i ++) {
            for (int j = 0; j < 16*2 - i*2-1; j++) {
                System.out.print(" ");
            }
    
            for (int j = 0; j <= i*2; j++) {
                System.out.print("* ");
            }
            System.out.println();
        }
    
        // star 步步为营, 上面找到规律后, 下面的是复制出来的
        
        
        for (int i = 0; i < 15; i ++) {
            for (int j = 0; j <= (i-1)*2; j++) {
                System.out.print(" ");
            }
            for (int j = 0; j < 16*2 - i*2-1; j++) {
                System.out.print(" *");
            }
            System.out.println();
        }

    }
}
