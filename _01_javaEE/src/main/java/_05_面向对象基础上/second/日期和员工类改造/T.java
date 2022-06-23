package _05_面向对象基础上.second.日期和员工类改造;/*
 *需求:
 （3）在测试类中的main中，创建两个员工对象，并为他们的姓名和生日赋值，并显示
 *要点:
 *      1
 *      2
 *      3
 */

public class T {
    public static void main(String[] args) {
        Employee employee = new Employee();
        Employee employee1 = new Employee();
    
        employee.setName("Jack");
        employee1.setName("Mike");
        employee.setBirthday(2000, 1, 1);
        employee1.setBirthday(2010, 1, 3);
    
        System.out.println(employee.getEmployInfo());
        System.out.println(employee1.getEmployInfo());
    }
}
