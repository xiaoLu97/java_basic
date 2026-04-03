package serialize;

import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        printObj();
    }
    public static void printObj() throws Exception {
        User user = new User(100, "John");
        // 序列化：把java对象存储到文件中
        OutputStream outputStream = new FileOutputStream("./userObj.txt");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(user);
        objectOutputStream.close();
        outputStream.close();
        // 反序列化：把文件中的java对象读取到内存中
        InputStream inputStream = new FileInputStream("./userObj.txt");
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        User user1 = (User) objectInputStream.readObject();
        System.out.println(user1);
        objectInputStream.close();
        inputStream.close();
    }
}
