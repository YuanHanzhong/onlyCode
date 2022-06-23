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
    

    
        // 添加
        // A
        dag.put("A", ANeibors);
        dag.put("B", BNeibors);
        dag.put("C", CNeibors);
        
        // 遍历拓扑
        tepo(dag,"A","A");
        
    }
    
    // 遍历图
    public static void tepo(HashMap<String, ArrayList<String>> dag, String vertex, String result) {
    
        if (vertex.equals("D") || vertex.equals("E")) {
            System.out.println( result);
        } else {
            for (String neighbor : dag.get(vertex)) {
                tepo(dag, neighbor, result+"-->"+neighbor);
            }
        }
    }
    
}
