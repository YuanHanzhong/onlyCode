package _03_dag;


import java.util.ArrayList;
import java.util.HashMap;

public class DAG {
    public static void main(String[] args) {
        // DAG
        HashMap<String, ArrayList<String>> dag = new HashMap<>();
        
        // ANeibors
        ArrayList<String> ANeibors = new ArrayList<>();
        ANeibors.add("B");
        ANeibors.add("C");
        
        // BNeibors
        ArrayList<String> BNeibors = new ArrayList<>();
        BNeibors.add("E");
        BNeibors.add("D");
    
        // CNeibors
        ArrayList<String> CNeibors = new ArrayList<>();
        CNeibors.add("E");
        CNeibors.add("D");
        CNeibors.add("F");
        
        // FNeibors
        ArrayList<String> FNeibors = new ArrayList<>();
        FNeibors.add("G");
        FNeibors.add("H");
        
        
        
        
    

    
        // 添加顶点
        // A
        dag.put("A", ANeibors);
        dag.put("B", BNeibors);
        dag.put("C", CNeibors);
        dag.put("F", FNeibors);
        
        // 遍历拓扑
        tepo(dag,"A","A");
        
    }
    
    // 遍历图
    // 定义参数是至关重要的一步 GOT
    
    /**
     *
     * @param dag 整个有向无环图
     * @param vertex 当前的顶点
     * @param result 已经有的结果
     */
    public static void tepo(HashMap<String, ArrayList<String>> dag, String vertex, String result) {
    
        // 递归调用, 先定义终止条件
        if (vertex.equals("D") || vertex.equals("E") || vertex.equals("G") || vertex.equals("H")) {
            System.out.println( result);
        } else {
            for (String neighbor : dag.get(vertex)) {
                tepo(dag, neighbor, result+"-->"+neighbor);
            }
        }
    }
    
}
