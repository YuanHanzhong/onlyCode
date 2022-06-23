package _12_泛型;

public class Test {
    @org.junit.Test
    public void chinese() {
        Student<String> stringStu = new Student<>("jack", "hao");
        System.out.println("stringStu = " + stringStu);
        
    }
    
    @org.junit.Test
    public void math() {
        Student<Double> mike = new Student<>("Mike", 89d);
        System.out.println("mike = " + mike);
    }
    
    @org.junit.Test
    public void chstu() {
        ChineseStudent chineseStudent = new ChineseStudent("He", "良");
        System.out.println("chineseStudent = " + chineseStudent);
        System.out.println("true = " + true);
    }
    
}
