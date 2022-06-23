package _09_多态._2;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Employee {
    private String name;
    private String gender;
    private int age;
    private double salary;
    
    public String getInfo() { // 这里借用了toString方法, 写起来很省力
        return "Employee{" +
                       "name='" + name + '\'' +
                       ", gender='" + gender + '\'' +
                       ", age=" + age +
                       ", salary=" + salary +
                       '}';
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public double getSalary() {
        return salary;
    }
    
    public void setSalary(double salary) {
        this.salary = salary;
    }
}
