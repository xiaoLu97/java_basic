package jucTool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
    public static void main(String[] args) {
        // 背景：ArrayList是线程不安全的，多线程并发 add 会导致数据不一致和状态异常。
        // ArrayList 底层是数组，扩容时需要先检查容量、再赋值。
        // 新数组的元素默认是 null。如果线程在扩容过程中读取数组，就会读到未被赋值的 null 元素。

        // 1 CyclicBarrier 加法计数器，满足就会执行后面的回调。执行后清0，可重复
//        countDownSample(); // 2 减法计数器
//        semaphoreSample(); // 3 计数信号量，用于限流操作。限制访问某些资源的线程数量。
        // 4 CopyOnWriteArrayList 写操作时，先复制一份，避免读写同时进行报异常
//        readWriteLockSample(); // 5 读写锁 可多线程读，但只有一个线程写
    }

    public static void countDownSample() {
        CountDownLatch countDownLatch = new CountDownLatch(50);
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                System.out.println("+++++++++++++++++++++++Thread");
                countDownLatch.countDown();
            }
        }).start();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 50; i++) {
            System.out.println("----------------------------Main");
        }
    }
    public static void semaphoreSample() {
        Semaphore semaphore = new Semaphore(5);
        for (int i = 0; i < 15; i++) {
            new Thread(() -> {
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName() + "进店购物");
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println(Thread.currentThread().getName() + "出店");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            }, String.valueOf(i)).start();
        }
    }
    public static void readWriteLockSample() {
        Cache cache = new Cache();
        for (int i = 0; i < 10; i++) {
            final int temp = i;
            new Thread(() -> {
                cache.write(temp, String.valueOf(temp));
                // cache.read(temp); 就算加在这里，也是写操作全部执行后，才执行读操作。说明写会阻塞读
            }).start();
        }

        for (int i = 0; i < 5; i++) {
            final int temp = i;
            new Thread(() -> {
                cache.read(temp);
            }).start();
        }
    }
}

class Cache {
    private final Map<Integer, String> map = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    // 写操作
    public void write(Integer key, String value) {
        lock.writeLock().lock();
        System.out.println(key + "开始写入");
        map.put(key, value);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println(key + "写入完毕");
        lock.writeLock().unlock();
    }

    // 读操作
    // 读锁的核心价值：和写锁互斥，杜绝 “读写同时进行”，避免脏读 / 数据不一致；
    // 读锁的性能优势：读 - 读共享，多个读线程可同时执行，比排他锁（如 synchronized）性能更高；
    // 最佳实践：解锁操作必须放在finally中，防止异常导致锁泄漏。
    public String read(Integer key) {
        lock.readLock().lock();
        System.out.println(key + "开始读取");
        String value = map.get(key);
//        System.out.println(key + "读取完毕");
        lock.readLock().unlock();
        return value;
    }
}