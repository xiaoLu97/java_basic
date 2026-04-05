package IOStream;

import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
//        diff();
//        readFile();
//        copyFile();
    }
    public static void copyFile() throws Exception {
        // 字符流无法复制 图片
        Reader reader = new FileReader("./test.txt");
        Writer writer = new FileWriter("./test_copy.txt");
        char[] chars = new char[1024];
        int len = reader.read(chars);
        if (len != -1) {
            writer.write(chars, 0, len);
        }
        reader.close();
        writer.close();
    }
    public static void readFile() throws Exception {
        Reader reader = new FileReader("./test.txt");
        char[] chars = new char[1024];
        int len = reader.read(chars);
        for (int i = 0; i < len; i++) {
            System.out.println(chars[i]);
        }

        reader.close();
    }
    public static void diff() throws Exception {
        // 字符流
        Reader reader = new FileReader("./test.txt");
        int temp = 0;
        while ((temp = reader.read()) != -1) {
            System.out.println(temp);
        }
        reader.close();

        System.out.println("**************************");
        // 字节流 1个汉字对应3个字节
        InputStream inputStream = new FileInputStream("./test.txt");
        int temp1 = 0;
        while ((temp1 = inputStream.read()) != -1) {
            System.out.println(temp1);
        }
        inputStream.close();
    }
}
