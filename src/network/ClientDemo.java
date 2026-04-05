package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientDemo {
    public static void main(String[] args) throws Exception {
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        socket = new Socket("127.0.0.1", 8080);
        System.out.println("------客户端------");
        //给服务器发消息
        String request = "你好";
        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeUTF(request);

        //接收服务器的消息
        inputStream = socket.getInputStream();
        dataInputStream = new DataInputStream(inputStream);
        String response = dataInputStream.readUTF();
        System.out.println("接收到服务器响应：" + response);

        inputStream.close();
        outputStream.close();
        dataInputStream.close();
        dataOutputStream.close();
        socket.close();
    }
}
