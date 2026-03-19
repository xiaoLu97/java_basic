package executorTool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Test {
    public static void main(String[] args) {
/*
        ThreadPoolExecutor 线程池的原生类
            corePoolSize: 核心池大小
            maximumPoolSize: 线程池最大线程数
            keepAliveTime: 空闲线程的存活时间
            unit: 时间单位
            workQueue: 阻塞队列
            threadFactory: 线程工厂
            handler: 拒绝策略
        超过最大线程数 + 队列长度就会报错
*/
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 1; i <= 10; i++) {
            final int num = i;
            // 提交任务到线程池
            executor.execute(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("线程名: " + threadName + ", i = " + num);
            });
        }

        // 关闭线程池
        executor.shutdown();
    }
}
