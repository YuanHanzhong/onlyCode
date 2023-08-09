package com.atguigu.java.chapter02;

import java.lang.reflect.Field;

/**
 * Java的字符串是不可变字符串
 *
 * 什么不可变?
 */
public class Java01_String {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        String str = " a b " ;
        System.out.println("trim前: !" + str + "!");  //  ! a b !

        String result = str.trim();

        System.out.println("trim后: !" + str + "!"); //  ! a b !

        System.out.println("trim后: !" + result + "!"); //  !a b!


        System.out.println("-----------------------------------------");

        // 改变字符串对象的内容
        // 反射
        String name = " a b " ;

        System.out.println(name );

        Class<? extends String> nameClass = name.getClass();

        Field valueField = nameClass.getDeclaredField("value");

        valueField.setAccessible(true);

        char [] cs  = (char[])valueField.get(name);

        cs[2] = '-' ;

        System.out.println(name );



    }
}
