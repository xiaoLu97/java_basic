# 乐观锁

根据你提供的文档内容，这里主要介绍了 JUC 中的几个同步工具（CountDownLatch、CyclicBarrier、Semaphore、读写锁和线程池），但**并没有直接介绍乐观锁**。

不过我可以为你解释一下什么是**乐观锁**，以及它与文档中提到的锁（如读写锁、ReentrantLock 等“悲观锁”）的区别：

### 1. 什么是乐观锁 (Optimistic Locking)？

乐观锁并不是 Java 中一个具体的类（像 `ReentrantReadWriteLock` 那样），而是一种**思想**或**机制**。

*   **核心思想**：它假设数据在大多数情况下不会产生冲突。因此，在读取数据时，它不会上锁，而是认为其他线程不会修改该数据。
*   **如何保证安全**：它在更新数据时会进行检查。通常使用 **CAS (Compare-And-Swap)** 算法或者**版本号机制 (Version)**。
    *   **版本号机制**：给数据加一个版本号。读取时读出数据和版本号；更新时，比较当前数据库中的版本号是否与之前读出的版本号一致。如果一致，则更新并递增版本号；如果不一致，说明数据已被其他线程修改，则放弃更新或重试。
    *   **CAS**：比较当前工作内存中的值和主内存中的值，如果相同则执行规定操作，否则继续比较直到主内存和工作内存中的值一致为止。

### 2. 乐观锁 vs 悲观锁 (Pessimistic Locking)

文档中提到的 `ReentrantReadWriteLock`、`synchronized` 以及 `Semaphore` 本质上更偏向于**悲观锁**的范畴（尽管读写锁做了优化）。

| 特性 | 悲观锁 (Pessimistic Lock) | 乐观锁 (Optimistic Lock) |
| :--- | :--- | :--- |
| **假设** | 假设最坏情况，每次去拿数据都认为别人会修改 | 假设最好情况，每次去拿数据都认为别人不会修改 |
| **处理方式** | 每次操作都先加锁，阻塞其他线程 | 不加锁，但在更新时判断是否被修改过 |
| **适用场景** | 写多读少（竞争激烈） | 读多写少（竞争较少） |
| **Java 实现** | `synchronized`, `ReentrantLock`, `ReentrantReadWriteLock` | `AtomicInteger`, `AtomicReference` (基于 CAS), 数据库中的 Version 字段 |
| **开销** | 线程上下文切换开销大，但能保证强一致性 | 避免了锁的开销，但如果冲突频繁，重试会导致性能下降 |

### 3. Java 中的乐观锁实现

在 Java JUC 包中，乐观锁的典型代表是 **`java.util.concurrent.atomic`** 包下的原子类，例如：

*   `AtomicInteger`
*   `AtomicLong`
*   `AtomicReference`

这些类底层使用了 **CAS (Unsafe 类)** 来实现线程安全，而没有使用传统的 `synchronized` 或 `Lock`。

#### 简单示例：AtomicInteger

```java
import java.util.concurrent.atomic.AtomicInteger;

public class OptimisticLockDemo {
    // 初始化值为 0
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                // incrementAndGet() 底层使用 CAS 算法，是乐观锁的体现
                int current = count.incrementAndGet();
                System.out.println(Thread.currentThread().getName() + ": " + current);
            }).start();
        }
    }
}
```


### 总结

*   **文档中的锁**（读写锁、信号量）：主要通过**阻塞线程**来保证同步，属于悲观策略。
*   **乐观锁**：通过**比较和交换 (CAS)** 或**版本号**来保证同步，不阻塞线程，适合读多写少的场景。

如果你想在项目中处理高并发下的计数器或状态更新，优先考虑 `Atomic` 系列类（乐观锁）；如果涉及复杂的代码块同步或写操作较多，则使用文档中提到的 `ReentrantLock` 或 `ReadWriteLock`（悲观锁）。

## JMM

在 Java 内存模型（JMM, Java Memory Model）中，"工作内存"和"主内存"是两个核心概念，它们描述了线程与数据之间的交互方式：

### 1. 主内存 (Main Memory)
*   **定义**：主内存是所有线程共享的内存区域。
*   **存储内容**：它主要存储了所有的**实例变量**、**静态变量**以及构成对象实例的数据。简单来说，你在代码里 `new` 出来的对象、类的静态字段等，本质上都存放在主内存中。
*   **特点**：它是数据的“源头”和“最终归宿”，对所有线程可见。

### 2. 工作内存 (Working Memory)
*   **定义**：工作内存是每个线程**私有**的内存区域。每个线程都有自己独立的工作内存，线程之间无法直接访问对方的工作内存。
*   **存储内容**：它存储了该线程使用到的变量的**副本**。这些变量是从主内存中拷贝过来的。
*   **特点**：线程对变量的所有操作（读取、赋值等）都必须在自己的工作内存中进行，而不能直接读写主内存中的变量。

---

### CAS 过程中的交互流程

当你提到 CAS（Compare-And-Swap）时，这个过程涉及到了这两个内存区域的交互：

1.  **读取阶段**：线程 A 从**主内存**中读取变量 `V` 的值，并将其拷贝到线程 A 自己的**工作内存**中，记为 `E`（Expected Value，预期值）。
2.  **计算阶段**：线程 A 在工作内存中对 `E` 进行一些计算，得到新值 `N`（New Value）。
3.  **CAS 阶段（比较并交换）**：
    *   线程 A 试图将工作内存中的新值 `N` 写回**主内存**。
    *   在执行写入之前，CAS 指令会进行一次原子性的检查：**“主内存中变量 `V` 当前的值，是否还等于我刚才读出来的预期值 `E`？”**
    *   **如果相等**：说明在此期间没有其他线程修改过这个变量，于是将主内存中的值更新为 `N`。
    *   **如果不相等**：说明有其他线程已经修改了主内存中的值，本次更新失败。线程 A 通常会重新从主内存读取最新的值，再次尝试上述过程（自旋）。

### 为什么要这样设计？
这种设计是为了提高性能。如果每次读写变量都要直接操作主内存（通常对应物理硬件的主存），速度会非常慢。通过让线程在私有的工作内存（通常对应 CPU 的高速缓存，如 L1/L2 Cache）中操作，可以极大地提升执行效率。

### 总结
*   **主内存** = 公共仓库（大家都能看到，但离得远，存取慢）。
*   **工作内存** = 个人办公桌（只有自己能用，离得近，存取快，但上面的东西只是仓库里的复印件）。
*   **CAS** = 在你把办公桌上的修改结果送回仓库时，先检查一下仓库里的原件有没有被别人动过。

## 示例

`AtomicInteger` 的输出结果具有两个核心特点：**最终值准确**，但**输出顺序不确定**。

### 1. 代码回顾
```java
import java.util.concurrent.atomic.AtomicInteger;

public class OptimisticLockDemo {
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                // incrementAndGet()：先加 1，再返回结果
                int current = count.incrementAndGet();
                System.out.println(Thread.currentThread().getName() + ": " + current);
            }).start();
        }
    }
}
```


### 2. 输出结果分析

#### A. 数值的准确性（不会出现重复或遗漏）
由于 `incrementAndGet()` 底层使用了 **CAS (Compare-And-Swap)** 乐观锁机制，它保证了原子性。
*   **结果**：控制台一定会输出从 `1` 到 `1000` 的所有整数。
*   **不会发生**：不会出现两个线程输出相同的数字，也不会跳过某个数字。这与使用普通 `int` 变量（会出现线程安全问题）形成鲜明对比。

#### B. 顺序的随机性（乱序输出）
虽然数值是连续的，但**输出的先后顺序是完全随机的**。
*   **原因**：你启动了 1000 个线程，操作系统的线程调度器决定哪个线程先获得 CPU 时间片。
*   **现象**：你可能会看到类似这样的输出：
    ```text
    Thread-5: 1
    Thread-12: 2
    Thread-1: 3
    Thread-99: 4
    ...
    Thread-888: 1000
    ```

    或者：
    ```text
    Thread-1: 1
    Thread-3: 2
    Thread-2: 3
    ...
    ```

    **注意**：`Thread-5` 输出 `1` 并不代表它是第一个启动的线程，只代表它是第一个成功执行完 `incrementAndGet` 并抢到 `System.out.println` 输出权的线程。

### 3. 为什么会有这种表现？

1.  **原子性保证**：`count.incrementAndGet()` 是一个不可分割的操作。即使多个线程同时调用，CAS 也会确保只有一个线程能成功修改主内存中的值，失败的线程会重试直到成功。因此，每个线程拿到的 `current` 值都是唯一的。
2.  **并发竞争**：`System.out.println` 本身也是一个同步方法（内部有锁），但线程到达这里的时机取决于 CPU 调度。因此，打印的顺序反映了线程执行的快慢和调度顺序，而不是线程创建的顺序。

### 4. 如果你想看“有序”的结果
如果你希望看到按顺序输出的结果，通常不能直接在线程里打印，而是需要等所有线程执行完毕后再统一处理，或者使用有序的数据结构收集结果。但在高并发场景下，**“结果正确”比“输出顺序”更重要**。

### 总结
*   **你会看到**：1000 行日志，每行包含一个线程名和一个从 1 到 1000 的唯一数字。
*   **你不会看到**：重复的数字、缺失的数字，或者固定的 `Thread-0, Thread-1...` 顺序。

这就是乐观锁（CAS）在解决多线程计数问题时的典型表现：**高效且安全，但不保证执行时序。**


# ThreadPoolExecutor 线程池

## executor.shutdown()

`shutdown()` 的作用是发出一个**“温和的停止信号”**，它的行为如下：

1.  **停止接收新任务**：调用后，如果你再尝试通过 `execute()` 或 `submit()` 提交新任务，线程池会抛出 `RejectedExecutionException`。
2.  **完成已有任务**：线程池会耐心等待，直到所有**已经提交**的任务（包括正在运行的和还在阻塞队列里排队的）全部执行完毕。
3.  **自动关闭**：当所有任务都完成后，线程池才会真正销毁线程并关闭。

所以，在你提供的代码中，即使 `shutdown()` 写在循环后面，那 10 个打印任务也**一定会全部执行完**，程序才会退出。

---

### 如果你想“强行”停止怎么办？

如果你希望不管任务有没有做完，立刻马上停止，应该使用 **`shutdownNow()`**：

*   **`shutdownNow()`**：
    *   尝试停止所有正在执行的任务（通过中断线程 `interrupt()`）。
    *   直接丢弃队列中还没开始执行的任务。
    *   返回那些还没来得及执行的任务列表。

### 如何确保任务真的执行完了？

虽然 `shutdown()` 会等待任务完成，但它是**异步**的（即调用完 `shutdown()` 后，主线程会继续往下走，不会卡在那里）。如果你需要在任务全部结束后再执行某些逻辑（比如关闭数据库连接），可以使用 `awaitTermination`：

```java
executor.shutdown(); // 先发起关闭请求
try {
    // 等待最多 60 秒，看线程池是否真的彻底关闭了
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        System.err.println("线程池任务未在 60 秒内完成！");
        executor.shutdownNow(); // 如果超时，再考虑强行关闭
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
    Thread.currentThread().interrupt();
}
System.out.println("所有任务已处理完毕，程序准备退出。");
```


### 总结

| 方法 | 行为特点 | 适用场景 |
| :--- | :--- | :--- |
| **`shutdown()`** | **优雅关闭**。不接新活，干完旧活再走。 | 绝大多数正常业务场景。 |
| **`shutdownNow()`** | **暴力关闭**。正在干的尽量停，没干的直接扔。 | 发生异常或需要紧急取消任务时。 |
| **`awaitTermination()`** | **阻塞等待**。配合 `shutdown()` 使用，用于确认关闭状态。 | 需要在主线程中同步等待所有子任务结束时。 |

## Callable接口和Future接口方式


