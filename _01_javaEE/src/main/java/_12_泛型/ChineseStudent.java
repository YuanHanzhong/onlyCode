package _12_泛型;

public class ChineseStudent extends Student<String> {
    public ChineseStudent(String name, String score) {
        super(name, score);
    }
}
