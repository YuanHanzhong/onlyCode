package _16_反射.左右看;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import java.lang.annotation.ElementType;

public class getClass {
    public static void main(String[] args) {
        //（1）基本数据类型和void
        Class c1 = int.class;
        Class c2 = void.class;
        
        System.out.println("c1 = " + c1);
        System.out.println("c2 = " + c2);
        //（2）类和接口
        Class c3 = String.class;
        Class c4 = Comparable.class;
        System.out.println("c3 = " + c3);
        System.out.println("c4 = " + c4);
        
        //（3）枚举
        Class c5 = ElementType.class;
        System.out.println("c5 = " + c5);
        
        //（4）注解
        Class c6 = Override.class;
        System.out.println("c6 = " + c6);
        
        //（5）数组
        Class c7 = int[].class;
        Class c9 = String[].class;
        Class c8 = int[][].class;
        System.out.println("c7 = " + c7);
        System.out.println("c8 = " + c8);
        System.out.println("c9 = " + c9);
    }
}
