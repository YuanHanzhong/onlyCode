package _09_多态._3;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Test {
    public static void main(String[] args) {
        Employee[] employees = new Employee[10]; // got 对象数组的定义
        
        employees[0] = new Employee();
        employees[1] = new Employee();
        employees[2] = new Employee();
        employees[3] = new Programmer();
        employees[4] = new Programmer();
        employees[5] = new Designer();
    
        for (int i = 0; i < 6; i++) {
            System.out.println(employees[i].getInfo());
    
        }
    }
}
