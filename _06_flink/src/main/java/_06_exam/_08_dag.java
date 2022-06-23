package _06_exam;

import java.util.ArrayList;
import java.util.HashMap;

public class _08_dag {
    public static void main(String[] args) {
        
        // vertex and neighbors
        ArrayList<String> ANeightbors = new ArrayList<>();
        ANeightbors.add("B");
        ANeightbors.add("C");
        
        ArrayList<String> BNeighbors = new ArrayList<>();
        BNeighbors.add("E");
        BNeighbors.add("F");
        
        ArrayList<String> CNeighbors = new ArrayList<>();
        CNeighbors.add("E");
        CNeighbors.add("F");
        
        // 构建dag, 关联vertex和neighbors
        HashMap<String, ArrayList<String>> dag = new HashMap<>();
        dag.put("A", ANeightbors);
        dag.put("B", BNeighbors);
        dag.put("C", CNeighbors);
        
        tepo(dag, "A", "A");
        
    }
    
    public static void tepo(HashMap<String, ArrayList<String>> dagMap, String vertex, String resultString) {
        if (vertex.equals("E") || vertex.equals("F")) {
            System.out.println("resultString = " + resultString);
        } else {
            for (String neighbor : dagMap.get(vertex)) {
                
                tepo(dagMap, neighbor, resultString + "-->" + neighbor);
            }
        }
    }
    
}
