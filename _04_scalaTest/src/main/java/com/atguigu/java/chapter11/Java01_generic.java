package com.atguigu.java.chapter11;

import java.util.ArrayList;
import java.util.List;

/**
 * 泛型
 */
public class Java01_generic {

    public static void main(String[] args) {
        //泛型:  声明 -> 不明确的类型.  使用 -> 用来明确(约束)类型。

        // 泛型基本使用：
        // 声明: ArrayList<E>
        // 使用：new ArrayList<String>
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("abc");
        //arrayList.add(123) ;

        String s = arrayList.get(0);

        ArrayList<Integer> arrayList1 = new ArrayList<>();
        arrayList1.add(1234) ;
        //arrayList1.add("abcd");
        Integer integer = arrayList1.get(0);


        ArrayList arrayList2 = new ArrayList<>();
        arrayList2.add(1234) ;
        arrayList2.add("abcd");
        Object o = arrayList2.get(0);


        // 类型擦除
        // 泛型是在编译期间用来约束类型 .

        ArrayList arrayList3 = new ArrayList<>();
        arrayList3.add("abcd");
        arrayList3.add(123);
        arrayList3.add(false) ;

        ArrayList<String> arrayList4 = arrayList3 ;

        String s1 = arrayList4.get(0);


        // 泛型 与 类型
        // 类型 -> 隐式转换
        // 泛型 -> 隐式转换 ×
        // 泛型上限   ? extends xxxx
        // 泛型下限   ? super xxxx
        ArrayList<String> arrayList5 = new ArrayList<>();
        //test(arrayList5) ;


        // 泛型不可变
        AAA<Parent> parentAAA = new AAA<Parent>();
        AAA<User> userAAA = new AAA<User>();
        AAA<SubUser> subUserAAA = new AAA<SubUser>();
        //AAA<Emp> empAAA = new AAA<Emp>();

        //泛型上限
        BBB bbb = new BBB();

        List<Parent> parentList = new ArrayList<Parent>();
        List<User> userList = new ArrayList<User>();
        List<SubUser> subUserList = new ArrayList<SubUser>();
        List<Emp> empList = new ArrayList<Emp>();

        //bbb.extendsTest(parentList);
        bbb.extendsTest(userList);
        bbb.extendsTest(subUserList);
        //bbb.extendsTest(empList);


        //泛型下限

        bbb.superTest(parentList);
        bbb.superTest(userList);
        //bbb.superTest(subUserList);
        //bbb.superTest(empList);


    }


//    public static void test(List<String> list ){
//        System.out.println("list.....");
//    }

    public static void test(ArrayList<Object> list){
        System.out.println("arrayList.....");
    }

}
class BBB{
    //上限  ? extends User   ? <=User
    public void extendsTest(List<? extends User> list) {

    }
    //下限 ? super User  ? >=User
    public void superTest(List<? super User> list){

    }
}



class AAA<T>{

}

class Parent{

}
class User extends Parent{

}

class SubUser extends User{

}

class Emp {

}
