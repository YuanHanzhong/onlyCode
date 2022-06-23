package _09_多态._3;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Employee {
    private int id;
    private String name;
    private int age;
    private double salary;
    
    public String getInfo() {
        return "Employee{" +
                       "id=" + id +
                       ", name='" + name + '\'' +
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
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
}
