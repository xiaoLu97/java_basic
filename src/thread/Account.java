package thread;

import java.util.concurrent.locks.ReentrantLock;

public class Account implements Runnable {
    private static int num;
    // 手动上锁，手动解锁，可以重复上锁
    // 限时性：可以判断某个线程在一定时间内能否获取到锁，具体使用tryLock()方法。超过时间拿不到返回false，不会一直等待
    // reentrantLock.isHeldByCurrentThread()：判断锁是否被当前线程持有
    private ReentrantLock reentrantLock = new ReentrantLock();
    @Override
    public void run() {
        reentrantLock.lock();
        num++;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "是第" + num + "位访客");
        reentrantLock.unlock();
    }
}
