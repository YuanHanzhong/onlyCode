package com.atguigu.java.chapter09;

//import com.atguigu.scala.chapter09.Scala01_exception;


public class Java01_exception {

    public static void main(String[] args) {
        // 处理异常的方式:  try... catch ...finally   throw / throws

        try {

            int a = 10;
            int b = 0;
            int c = a / b;

        } catch (ArithmeticException e){
            //异常的处理过程
            e.printStackTrace();

        } catch (Exception e){
            e.printStackTrace();

        } finally {

            System.out.println("finally");
        }


        //testException();
        // 编译时异常
        // 运行时异常

        //new FileInputStream(new File("d:\\aaa"));

        //Scala01_exception.testException();

    }

    public static void testException() throws Exception{

        throw new RuntimeException("haha");
    }

}
