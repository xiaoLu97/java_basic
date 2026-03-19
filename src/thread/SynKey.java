package thread;

public class SynKey {
    public static void main(String[] args) {
        // synchronized修饰非静态方法，锁定的是方法的调用者，即实例，而不是实例的方法。syn fun1， syn fun2。进入fun1锁定后，fun2需要等待。（前提：被syn修饰）

        // synchronized可以修饰静态方法，实例方法，代码块。
        // 如何判断多个线程是否会实现同步，看锁定的资源在内存中有几份，如果只有一份，多线程肯定需要排队，如果多份则不需要排队，就不会实现同步。
        SynKey t = new SynKey();
        SynKey t1 = new SynKey();
        SynKey t2 = new SynKey();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
//                SynKey t = new SynKey();
                t.test();
            });
            thread.start();
        }
    }

    public void test() {
        synchronized (this) {
            System.out.println("start");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("end");
        }
    }
}
