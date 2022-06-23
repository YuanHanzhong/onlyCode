package _06_exam;

import java.util.ArrayList;
import java.util.HashMap;

public class _04_dag {
    public static void main(String[] args) {
        HashMap<String, ArrayList<String>> dag = new HashMap<>();
        
        ArrayList<String> Aneighbors = new ArrayList<>();
        Aneighbors.add("B");
        Aneighbors.add("C");
        
        ArrayList<String> Bneighbors = new ArrayList<>();
        Bneighbors.add("E");
        Bneighbors.add("F");
        
        ArrayList<String> Cneighbors = new ArrayList<>();
        Cneighbors.add("E");
        Cneighbors.add("F");
        
        dag.put("A", Aneighbors);
        dag.put("B", Bneighbors);
        dag.put("C", Cneighbors);
        
        getPrint(dag, "A", "A");
    }
    
    public static void getPrint(HashMap<String, ArrayList<String>> dag, String vertex, String result) {
        if (vertex.equals("E") || vertex.equals("F")) {
            String watch = result;
            System.out.println("result = " + watch);
        } else {
            for (String neighbor : dag.get(vertex)) {
                getPrint(dag, neighbor, result +"-->"+neighbor);
            }
        }
    }
    
}
