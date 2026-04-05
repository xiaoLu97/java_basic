package network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class TerminalA {
    public static void main(String[] args) throws Exception {
        byte[] buff = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(buff, buff.length);
        DatagramSocket datagramSocket = new DatagramSocket(8001);
        datagramSocket.receive(datagramPacket);
        String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        System.out.println("我是TerminalA，接收到了"+datagramPacket.getPort()+"传来的数据："+message);

        String reply = "我是TerminalA，已经接收到你发来的数据";
        SocketAddress socketAddress = datagramPacket.getSocketAddress();
        // 数据包的形式传输
        DatagramPacket datagramPacket1 = new DatagramPacket(reply.getBytes(), reply.getBytes().length, socketAddress);
        datagramSocket.send(datagramPacket1);
    }
}
