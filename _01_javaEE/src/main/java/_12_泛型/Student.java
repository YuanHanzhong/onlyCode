package _12_泛型;

public class Student<T>{
    String name;
    T score;
    
    @Override
    public String toString() {
        return "Stu{" +
                 "name='" + name + '\'' +
                 ", score=" + score +
                 '}';
    }
    
    public T getScore() {
        return score;
    }
    
    public void setScore(T score) {
        this.score = score;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Student(String name, T score) {
        this.name = name;
        this.score = score;
    }
}
