package _09_多态._2;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Person {
    private String name;
    private int age;
    private int career;
    private String gender; // 判断性别, 根本不用字符串
    
    
    public String getGender() {
        return gender;
    }
    
    
    
    public void eat() {
        System.out.println(name+", 吃饭啦!");
    }
    
    public void toilet() {
        System.out.println(name + ", 上厕所!");
    }
    public String getInfo() {
        return "Person{" +
                       "name='" + name + '\'' +
                       ", age=" + age +
                       ", career=" + career +
                       '}';
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public int getCareer() {
        return career;
    }
    
    public void setCareer(int career) {
        this.career = career;
    }
}
