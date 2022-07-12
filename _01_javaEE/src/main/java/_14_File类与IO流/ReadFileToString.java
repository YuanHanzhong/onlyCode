package _14_File类与IO流;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class ReadFileToString {
    
    /**
     * 此类显示将完整文件内容读取到String的不同方法
     *
     * @param args
     *
     * @throws IOException
     */
    public static void main(String[] args) {
        String fileName = "D:\\onedrive\\01_正吸收\\014_专业\\only_code\\_01_javaEE\\src\\main\\resources\\test.sql";
        
        String contents = readUsingScanner(fileName);
        System.out.println("*****Read File to String Using Scanner*****\n" + contents);
        
        contents = readUsingApacheCommonsIO(fileName);
        System.out.println("*****Read File to String Using Apache Commons IO FileUtils*****\n" + contents);
        
        contents = readUsingFiles(fileName);
        System.out.println("*****Read File to String Using Files Class*****\n" + contents);
        
        contents = readUsingBufferedReader(fileName);
        System.out.println("*****Read File to String Using BufferedReader*****\n" + contents);
        
        contents = readUsingBufferedReaderCharArray(fileName);
        System.out.println("*****Read File to String Using BufferedReader and char array*****\n" + contents);
        
        contents = readUsingFileInputStream(fileName);
        System.out.println("*****Read File to String Using FileInputStream*****\n" + contents);
        
    }
    
    private static String readUsingBufferedReaderCharArray(String fileName) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[10];
        try {
            reader = new BufferedReader(new FileReader(fileName));
            while (reader.read(buffer) != -1) {
                stringBuilder.append(new String(buffer));
                buffer = new char[10];
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        
        return stringBuilder.toString();
    }
    
    private static String readUsingFileInputStream(String fileName) {
        FileInputStream fis = null;
        byte[] buffer = new byte[10];
        StringBuilder sb = new StringBuilder();
        try {
            fis = new FileInputStream(fileName);
            
            while (fis.read(buffer) != -1) {
                sb.append(new String(buffer));
                buffer = new byte[10];
            }
            fis.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return sb.toString();
    }
    
    private static String readUsingBufferedReader(String fileName) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line = null;
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            // delete the last ls
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        
        return stringBuilder.toString();
    }
    
    private static String readUsingFiles(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String readUsingApacheCommonsIO(String fileName) {
        try {
            return FileUtils.readFileToString(new File(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String readUsingScanner(String fileName) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(Paths.get(fileName), StandardCharsets.UTF_8.name());
            // 可以使用Delimiter正则表达式 "\\A", "\\Z" or "\\z"
            String data = scanner.useDelimiter("\\A").next();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (scanner != null)
                scanner.close();
        }
        
    }
    
}
//更多请阅读：https://www.yiibai.com/java/java-read-file-to-string.html
