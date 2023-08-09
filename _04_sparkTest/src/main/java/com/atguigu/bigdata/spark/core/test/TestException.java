package com.atguigu.bigdata.spark.core.test;

public class TestException {
    public static void main(String[] args) {
        // TODO 空指针异常：调用一个为空（null）对象的成员属性或成员方法时，所产生得异常
        // 异常是在运行时产生的，所以查找异常应该在字节码中。
        //User user = null;
        // Integer => Integer.intValue(拆箱) => int
        //test(user.age);

        // int => Integer.valueOf(装箱) => Integer
//        Integer i1 = 127;
//        Integer i2 = 127;
//
//        System.out.println(i1 == i2); // true

    }
    public static void test( int age ) {
        System.out.println(age);
    }
}
class User {
    public static Integer age;
}
