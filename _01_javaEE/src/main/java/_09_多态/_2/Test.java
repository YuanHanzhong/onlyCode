package _09_多态._2;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

public class Test {
    public static void main(String[] args) {
        //Scanner scanner = new Scanner(System.in);
        //
        //Employee employee = new Employee();
        //
        //System.out.println("age: ");
        //employee.setAge(scanner.nextInt());
        //
        //scanner.nextLine();
        //System.out.println("gender: ");
        //employee.setGender(scanner.nextLine());
        //
        //System.out.println("name: ");
        //employee.setName(scanner.nextLine());
        //
        //System.out.println("salary: ");
        //employee.setSalary(scanner.nextInt());
        //
        //System.out.println(employee.getInfo());
        //
        //
        // ------------------------------Man Woman Person HDFS上传下载.test--------------------
        Person[] people=new Person[10];
        
        people[0] = new Man(); // 不用再在前面写类型了 got
        people[1] = new Man();
    
        people[2] = new Woman();
        people[3] = new Woman();
    
        for (int i = 0; i < 3; i++) {
            System.out.println(people[i].getInfo());
            people[i].eat();
            people[i].toilet();
            //if (people[i].getGender() == "man") {
            //    ((Man)people)[i].// 强转类型
            //}
        // got if(父类 instance of 子类), 可定用的是父类的类型判断的,
            // got 强转
            
            if (people[i] instanceof Man) {
                Man man = (Man) people[i]; // 父类变为子类的套路
                man.smoke();
            } else if (people[i] instanceof Woman) {
                Woman woman = (Woman) people[i];
                woman.makeup();
            }
        }
        
    }
}
