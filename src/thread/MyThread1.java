package thread;

public class MyThread1 extends Thread {
    @Override
    public void run() {
        for (int i = 1; i <= 50; i++) {
            System.out.println("MyThread1: " + i);
        }
    }
}
