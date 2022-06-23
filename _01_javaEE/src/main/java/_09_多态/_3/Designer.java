package _09_多态._3;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Designer extends Programmer {
    int bonus;
    
    @Override
    public String getInfo() {
        System.out.println("我是架构师");
        return super.getInfo();
        
    }
}
