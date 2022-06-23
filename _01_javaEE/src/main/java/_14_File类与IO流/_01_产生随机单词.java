package _14_File类与IO流;/*
 *需求:
 *要点:
 *      1
 *      2
 *      3
 */

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class _01_产生随机单词 {
    public static void main(String[] args) throws IOException {
        String[] words = {"dog", "cat", "man", "woman"};
        
        // 准备环境
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("f:/word.txt"));
        
        // 处理逻辑
        for (int i = 0; i < 10000000; i++) {
            int index = (int) (Math.random() * words.length);
            bufferedOutputStream.write(words[index].getBytes(StandardCharsets.UTF_8));
            if (i % 10 == 0) {
                bufferedOutputStream.write('\n');
            } else {
                bufferedOutputStream.write(' ');
            }
        }
        
        // 关闭
        bufferedOutputStream.close();
    }
}
