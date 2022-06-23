package _09_多态._2;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Woman extends Person {
    @Override
    public void eat() {
        System.out.println(getName()+"细嚼慢咽");
    }
    
    public void makeup() {
        System.out.println(getName()+" 化妆");
    }
}
