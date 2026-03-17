package thread;

public class Main {
    public static void main(String[] args) {
        MyThread1 thread1 = new MyThread1();
        MyThread2 thread2 = new MyThread2();
        // 将接口作为参数传递的场景下，且接口只有一个方法，可以使用lambda进行简化
        Thread thread3 = new Thread(()->{
            for (int i = 1; i <= 50; i++) {
                System.out.println("Lambda: " + i);
            }
        });
        thread1.start();
        thread2.start();
        thread3.start();

        try {
            // 在哪个线程中调用sleep方法就让哪个线程休眠，和调用者无关
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= 50; i++) {
            System.out.println("Main: " + i);
        }
    }
}
