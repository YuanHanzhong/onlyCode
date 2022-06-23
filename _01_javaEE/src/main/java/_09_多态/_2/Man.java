package _09_多态._2;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Man extends Person {
    @Override
    public void eat() {
        System.out.println(getName() + ", 狼吞虎咽");
        
    }
    
    public void smoke() {
        System.out.println(getName()+", 抽烟");
    }
}
