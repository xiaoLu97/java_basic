package network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TerminalB {
    public static void main(String[] args) throws Exception {
        String message = "我是TerminalB，你好！";
        InetAddress inetAddress = InetAddress.getByName("localhost");
        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, 8001);
        DatagramSocket datagramSocket = new DatagramSocket(8002);
        datagramSocket.send(datagramPacket);

        byte[] buff = new byte[1024];
        DatagramPacket datagramPacket1 = new DatagramPacket(buff, buff.length);
        datagramSocket.receive(datagramPacket1);
        String response = new String(datagramPacket1.getData(), 0, datagramPacket1.getLength());
        System.out.println("我是TerminalB，接收到了"+datagramPacket1.getPort()+"发来的数据："+response);
    }
}
