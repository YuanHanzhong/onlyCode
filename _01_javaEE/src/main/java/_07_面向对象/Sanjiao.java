package _07_面向对象;/*
 *需求:声明一个三角形类Triangle，包含属性：a,b,c，都是double类型，表示三条边，包含几个方法：

-   boolean isTriangle()：判断是否是一个三角形
-   boolean isRightTriangle()：判断是否是一个直角三角形
-   boolean isIsoscelesTriangle()：判断是否是一个等腰三角形
-   boolean isEquilateralTriangle()：判断是否是一个等边三角形
-   double area()：根据三条边，用海伦公式求面积
-   double perimeter()：求周长

 *要点:
 *      1
 *      2
 *      3
 */

public class Sanjiao {
    double a;
    double b;
    double c;
    
    boolean isTriangle() {
        if (a + b > c && a + c > b && b + c > a) {
            return true;
        } else{
            return false;
        }
    }
    
    boolean isRightTriangle() {
        if (a * a + b * b == c * c || b * b + c * c == a * a || a * a + c * c == b * b) {
            return true;
        } else {
            return false;
        }
    }
    

    
    
}
