package _11_递归;

import org.junit.Test;

public class feibo {
    public static void main(String[] args) {
    
    }
    
    /**
     * 使用递归的方式, 动态规划, GOT // NOTE 2022/9/8
     * 实现 1+1+2+3+5
     * 新得
     * 递归, 要调用自己,
     */
    
    public static long fabo(int endNumber) {
        // 递归就是先写结束条件
        if (endNumber == 1 ) {
            return 1;
        }
        if (endNumber == 2) {
            return 2;
        }
        // 慢慢消耗
        return fabo(endNumber - 1) + fabo(endNumber - 2);
    }
    
    @Test
    public void test1() {
    
        System.out.println("fabo(3) = " + fabo(3));
    
        System.out.println("fabo(5) = " + fabo(5));
    
        System.out.println("fabo(6) = " + fabo(6));
    }
    
    /**
     * 使用数组的方式
     */
    
    
    @Test
    public void test2() {
        // NOTE 2022/9/8 定义数组是需要 new 的
    
        int[] fabo = new int[9];
        fabo[0]=1;
        fabo[1]=1;
        for (int i = 2; i < 8; i++) {
            fabo[i] = fabo[i - 1] + fabo[i - 2];
        }
    
        // NOTE 2022/9/8 数组就是要特别考虑边界问题
        System.out.println("fabo[8] = " + fabo[6]);
        
    }
    
}
