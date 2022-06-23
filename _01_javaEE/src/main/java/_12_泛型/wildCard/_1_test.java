package _12_泛型.wildCard;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/*
案例：
声明一个集合工具类MyCollections，要求包含：
- public static boolean different(Collection\<?> c1, Collection<?> c2)：比较两个Collection集合，此时两个Collection集合的泛型可以是任意类型，如果两个集合中没有相同的元素，则返回true，否则返回false。
- public static <T> void addAll(Collection<? super T> c1, T... args)：可以将任意类型的多个对象添加到一个Collection集合中，此时要求Collection集合的泛型指定必须>=元素类型。
- public static <T> void copy(Collection<? super T> dest,Collection<? extends T> src)：可以将一个Collection集合的元素复制到另一个Collection集合中，此时要求原Collection泛型的类型<=目标Collection的泛型类型。

 */
public class _1_test {
    /*
    比较两个Collection集合，此时两个Collection集合的泛型可以是任意类型，如果两个集合中没有相同的元素，则返回true，否则返回false。
    c1 => {"hello","world","java"}
    c2 => {"hello","world","h5"}
    c1和c2它们不同
    c3 => {"hello","world","java"}
    c1和c3相同

    c4 => {"hello","world","java","haha"}
    c1所有元素都可以在c4中找到
     */
    public static boolean different(Collection<?> c1, Collection<?> c2) {
/*        for (Object o : c1) {
            //判断c1的元素是否在c2中包含，如果有不包含的，就不相同
            if(!c2.contains(o)){
                return false;
            }
        }
        for(Object o : c2){
            //判断c2的元素是否在c1中包含，如果有不包含的，就不相同
            if(!c1.contains(o)){
                return false;
            }
        }

        return true;*/
        
        return c1.containsAll(c2) && c2.containsAll(c1);
    }
    
    @Test
    public void test01() {
        Collection<String> c1 = new ArrayList<>();
        c1.add("hello");
        c1.add("world");
        c1.add("java");
        
        Collection<String> c2 = new ArrayList<>();
        c2.add("hello");
        c2.add("world");
        c2.add("h5");
        
        Collection<String> c3 = new ArrayList<>();
        c3.add("hello");
        c3.add("world");
        c3.add("java");
        
        Collection<String> c4 = new ArrayList<>();
        c4.add("hello");
        c4.add("world");
        c4.add("java");
        c4.add("h5");
        
        Collection<Integer> c5 = new ArrayList<>();
        c5.add(1);
        c5.add(2);
        
        
        System.out.println(different(c1, c2));
        System.out.println(different(c1, c3));
        System.out.println(different(c1, c4));
        System.out.println(different(c1, c5));
    }
    
    /*
    可以将任意类型的多个对象添加到一个Collection集合中，此时要求Collection集合的泛型指定必须>=元素类型。
    Collection<? super T> c1：因为Collection的元素类型可以>=T
     */
    public static <T> void addAll(Collection<? super T> c1, T... args) {
        for (int i = 0; i < args.length; i++) {
            c1.add(args[i]);
        }
    }
    
    public static <T> T addAll2(Collection<? super T> c1, T... args) {
        for (int i = 0; i < args.length; i++) {
            c1.add(args[i]);
        }
        return args[0];
    }
    
    @Test
    public void test02() {
        Collection<Object> coll = new ArrayList<>();
        addAll(coll, "hello", "java", "world");
        System.out.println(coll);
    }
    
    @Test
    public void test03() {
        Collection<String> coll = new ArrayList<>();
        addAll(coll, "hello", "java", "world");
        System.out.println(coll);
    }
    
    /*
    要求把src集合的元素全部复制到dest集合中。
    这两个集合的元素类型应该满足什么要求？
    （1）相同：可以
    （2）dest集合的元素类型 > src集合的元素类型
    （3）dest集合的元素类型 < src集合的元素类型（错误）
     */
    public static <T> void copy(Collection<? super T> dest, Collection<? extends T> src) {
        for (T t : src) {
            dest.add(t);
        }
    }
    
    @Test
    public void test04() {
        Collection<Object> dest = new ArrayList<>();
        Collection<String> src = new ArrayList<>();
        src.add("hello");
        src.add("java");
        copy(dest, src);
        System.out.println(dest);
    }
    
    @Test
    public void test05() {
        Collection<Number> dest = new ArrayList<>();
        Collection<Integer> src1 = new ArrayList<>();
        src1.add(1);
        src1.add(2);
        copy(dest, src1);
        
        Collection<Double> src2 = new ArrayList<>();
        src2.add(1.0);
        src2.add(2.0);
        copy(dest, src2);
        System.out.println(dest);
    }
}
