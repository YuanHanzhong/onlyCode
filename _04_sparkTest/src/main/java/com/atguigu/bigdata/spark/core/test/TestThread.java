package com.atguigu.bigdata.spark.core.test;

public class TestThread {
    public static void main(String[] args) throws Exception {

        Thread t1 = new Thread();
        Thread t2 = new Thread();

        t1.start();
        t2.start();

        // 核心区别：字体不一样
        t1.sleep(1000); // 静态方法，和类有关，和对象没有关系，t1线程没有休眠，main线程休眠
        t2.wait(); // 成员方法，和对象相关， t2线程在等待

    }
}
