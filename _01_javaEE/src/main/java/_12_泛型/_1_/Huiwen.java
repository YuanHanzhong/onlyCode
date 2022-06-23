package _12_泛型._1_;

public class Huiwen{
    public static boolean isHuiwen(String s) {
        char[] chars = s.toCharArray();
        boolean flag = true;
    
        for (int i = 0; i < chars.length / 2; i++) {
            if (chars[i] != chars[s.length() - 1 - i]) {
                flag = false;
                break;
            }
        }
        return flag;
    }
}
