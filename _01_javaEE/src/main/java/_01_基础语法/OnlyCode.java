package _01_基础语法;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import org.junit.Test;

public class OnlyCode {
    public static void main(String[] args) {
        System.out.println("OnlyCodd = ");
        
        // 2>  println要换行, 需要借助\n
        System.out.println("善学如春起之苗, 不见其增, 日有所长.\n" +
                                   "假学如磨刀之石, 不见其损, 年有所亏\n");
        
        boolean is = true;
        boolean not = false;
        
        System.out.println(true);
        System.out.println(false);
        
        
        Boolean Bis = Boolean.TRUE;
        System.out.println(Bis);
        
        int a = 2  << 8;
        System.out.println("a = " + a);
        
        float pi = 3.14f;
        // got float定义时, 后面跟f, double时, 不写, 或者写L
    
        System.out.println(pi);
        
        double fupi = -3.14;
        System.out.println("fupi = " + fupi);
        
        int b = 5;
        int c = a;
        a = b;
        b =c;
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("c = " + c);
        
        // 基本数据类型, 8个, 连数据类型都是2的整数倍 got
        // 自己想上来的: byte  int long , float double , boolean , char
        // 忘记的: char long short got
        // 错了的: String不是基本数据类型. String和[]都是引用类型, 存储的是地址.
        
        char c1 = '国'; // char下可以存汉字, 但要用单引 got
        System.out.println("c1 = " + c1);
        
        
        float f = 8.9f;
        System.out.println(f);
        System.out.println("f = " + f);
        
        float UpperF = 8.9F;
        System.out.println("UpperF = " + UpperF);
        //UpperF.getClass // 怎么通过变量获取类型
        //System.out.println("UpperF.class = " + UpperF.getClass());
        
        long Lo = 88L;
        long lo = 99l;
    
        System.out.println("Lo = " + Lo);
        System.out.println("lo = " + lo);
        
        char cc = '忠'; // 看某个字符的assic
        System.out.println("cc = " + (int)cc); // 强制类型转换, 是再前面(), 相当于标注下, 只不过是标注在了前面 got  朝闻道, 夕死可以.
    
        System.out.println(2<<8);
    

        
        
    
    
    }
    
    @Test
    public void test () {
        /**
         * 题目：2 乘以8
         * 要点：
         */
        System.out.println(2<<3);
    
        System.out.println(2<<4);
    
        System.out.println(Math.round(11.2)); //11
        System.out.println(Math.round(-11.2)); //-11
        System.out.println(Math.round(-11.9)); //-11
    
        char c = 'x';
        System.out.println(true?c:'y');
    

        
    }
    @Test
    public void test2 () {
        char x = 'x';
        int i = 10;
        System.out.println("x = " + x);
        System.out.println(true? x : i);
        System.out.println(false? x:i);
        System.out.println(true? 'x' : 10);
    }
    
    
}
