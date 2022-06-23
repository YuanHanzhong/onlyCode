package _16_反射.exercise;/*
 *需求:// 2022年2月21日

（1）声明一个尚硅谷学生类AtguiguStudent，

-   包含静态变量：学校school
-   包含属性：班级名称className
-   并提供构造器，get/set等

-   实现Serializable
-   实现Comparable<T>接口，重写int compareTo(T t)方法按照班级名称排序

（2）在测试类的test01()测试方法中，用反射获取AtguiguStudent类的Class对象，并获取它的所有信息，包括类加载器、包名、类名、父类、父接口、属性、构造器、方法们等。

（3）在测试类的test02()测试方法中，用反射设置school的值为“尚硅谷”，获取school的值

（4）在测试类的test03()测试方法中，用反射创建AtguiguStudent类的对象，并设置班级名称className属性的值，并获取它的值

（5）在测试类的test04()测试方法中，用反射获取有参构造创建2个AtguiguStudent类的对象，并获取compareTo方法，调用compareTo方法，比较大小。

 *要点:
 *      1
 *      2
 *      3
 */
public class Student {
    private String school = "NC"; //
    String classNme ;
    
    public String getSchool() {
        return school;
    }
    
    public void setSchool(String school) {
        this.school = school;
    }
    
    public String getClassNme() {
        return classNme;
    }
    
    public void setClassNme(String classNme) {
        this.classNme = classNme;
    }
}

