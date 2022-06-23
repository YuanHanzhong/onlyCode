package _12_泛型;

public class Rec implements Comparable<Rec> {
    double width;
    double length;
    
    public double area() {
        return width * length;
    }
    
    public double perimeter() {
        return(width + length) * 2;
    }
    
    
    
    @Override
    public String toString() {
        return "Rec{" +
                 "width=" + width +
                 ", length=" + length +
                 ", area= "+area()+
                 ", perimeter= "+perimeter()+
                 '}';
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
    }
    
    public double getLength() {
        return length;
    }
    
    public void setLength(double length) {
        this.length = length;
    }
    
    public Rec(double width, double length) {
        this.width = width;
        this.length = length;
    }
    
    public Rec(double width) {
        this.width = width;
    }
    
    @Override
    
    public int compareTo(Rec o) {
        boolean flag;
        // 先比较面积, 再比较周长
    
        int compareResult = Double.compare(this.area(), o.area());
        if (compareResult == 0) {
            return Double.compare(this.perimeter(), o.perimeter());
        } else {
            return compareResult;
        }
    }
}
