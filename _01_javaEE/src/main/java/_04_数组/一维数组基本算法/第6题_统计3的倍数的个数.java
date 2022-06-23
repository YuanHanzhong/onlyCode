package _04_数组.一维数组基本算法;/*
 *需求: 随机产生10个[0,100)之间整数放到一维数组中，然后遍历所有随机数，并统计随机数中满足3的倍数有几个。
 *要点:
 *      1
 *      2
 *      3
 */

public class 第6题_统计3的倍数的个数 {
    public static void main(String[] args) {
        
        int count=0;
        int[] a = new int[10];
        for (int i = 0; i < a.length; i++) {
            a[i] = (int)(Math.random()*100); // 注意, 随机数, 先 * 再转换
            System.out.println("a[i] = " + a[i]);
            if (a[i] % 3 == 0) {
                count++;
            }
        }
        System.out.println("3的倍数的数有: " + count);
        
        // 冒泡排序
        for (int i = 0; i < a.length - 1; i++) {
            boolean isSorted = true;
            for (int j = 0; j < a.length - 1 - i; j++) {
                if (a[j] < a[j + 1]) {
                    int temp= a[j];
                    a[j] = a[j + 1];
                    a[j+1] = temp;
                    isSorted = false;
                }
            }
            if (isSorted) {
                break;
            }
        }
    
        for (int i : a) {
            System.out.println("排序后: " + i);
        }
    }
}
