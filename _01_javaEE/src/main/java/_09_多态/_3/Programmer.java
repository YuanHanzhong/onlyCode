package _09_多态._3;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Programmer extends Employee {
    
    @Override
    public String getInfo() {
        System.out.println("我是程序员");
        
        return super.getInfo();
        
    }
}
