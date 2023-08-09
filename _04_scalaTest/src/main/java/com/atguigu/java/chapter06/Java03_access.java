package com.atguigu.java.chapter06;

public class Java03_access /*extends Object */ {

    public void test() throws CloneNotSupportedException {
        this.clone();

    }


    public static void main(String[] args) {
        JavaUser03 user03 = new JavaUser03();
        //user03.clone();


        /*
            protected : 本类  本包  子类

            方法的提供者 :  java.lang.Object

            方法的访问位置 :  com.atguigu.java.chapter06.Java03_access


         */
    }
}

class JavaUser03 /*extends Object*/ {

    public void test() throws CloneNotSupportedException {
        this.clone() ;
    }

}