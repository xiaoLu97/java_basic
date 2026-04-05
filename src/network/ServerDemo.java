package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDemo {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = null;
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        serverSocket = new ServerSocket(8080);
        System.out.println("-----服务端-----");
        System.out.println("已启动，等待接收客户端请求...");

        while (true) {
            socket = serverSocket.accept(); // 一次通话操作

            inputStream = socket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);
            String request = dataInputStream.readUTF();
            System.out.println("接收到客户端请求：" + request);

            String response = "Hello World";
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF(response);

//            inputStream.close();
//            outputStream.close();
//            dataInputStream.close();
//            dataOutputStream.close();
//            socket.close();
//            serverSocket.close();
        }
    }
}
