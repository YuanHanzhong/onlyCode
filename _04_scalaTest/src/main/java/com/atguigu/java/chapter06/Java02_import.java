package com.atguigu.java.chapter06;

import static com.atguigu.java.chapter06.TestImportStatic.* ;
public class Java02_import {
    public static void main(String[] args) {
        //System.out.println(TestImportStatic.NAME);
        //TestImportStatic.test();

        System.out.println(NAME);
        test();
    }

}


class TestImportStatic{

    public static String NAME = "admin" ;

    public static void test(){
        System.out.println("test");
    }

}