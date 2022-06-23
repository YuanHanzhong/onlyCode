package _09_多态._2;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Child extends Father{
    public String grade;
    
    public static void main(String[] args){
        Father f = new Child();
        System.out.println(f.name);
    }
}