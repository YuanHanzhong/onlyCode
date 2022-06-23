package _05_面向对象基础上.第一次练习;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Circle {
    private double r;
    
    public void setR(double r) {
        this.r = r;
    }
    
    public void showInfo() {
        System.out.println("半径为: "+r+ "周长: "+2*Math.PI*r + "面积: "+ Math.PI*r*r);
    }
}
