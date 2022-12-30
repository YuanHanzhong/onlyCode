package _01_基础语法;

import java.util.HashMap;
import java.util.Map;

public class WordCount {
    public static void main(String[] args) {
        String text = "The The The "
;
        
        // 移除 "|" 符号
        text = text.replace("|", "");
        
        // 拆分文本为单词
        String[] words = text.split(" ");
        
        // 统计每个单词的出现次数
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            if (wordCount.containsKey(word)) {
                wordCount.put(word, wordCount.get(word) + 1);
            } else {
                wordCount.put(word, 1);
            }
        }
        
        // 输出每个单词的出现次数
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            System.out.println(entry.getKey() + " -- " + entry.getValue());
        }
    }
}
