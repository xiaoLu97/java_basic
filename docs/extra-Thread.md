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


# **ThreadPoolExecutor vs ThreadPoolTaskExecutor 核心区别**

## **一、基本定位**

**ThreadPoolExecutor**
- JDK 原生线程池实现类（`java.util.concurrent.ThreadPoolExecutor`）
- Java 并发包的基础组件
- 所有线程池的底层实现

**ThreadPoolTaskExecutor**
- Spring Framework 提供的封装类（`org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor`）
- 对 `ThreadPoolExecutor` 的包装和增强
- 专为 Spring 生态设计

---

## **二、配置方式差异**

**ThreadPoolExecutor**
- 通过构造函数传入所有参数
- 参数顺序固定，需要一次性传完 7 个参数
- 配置完成后即可使用

**ThreadPoolTaskExecutor**
- 通过 Setter 方法逐个设置参数
- 配置更灵活，可以分步设置
- **必须调用 `initialize()` 方法**才能完成初始化

---

## **三、关键差异点**

### **1. Spring 生态集成度**

| 特性 | ThreadPoolExecutor | ThreadPoolTaskExecutor |
|------|-------------------|----------------------|
| **实现接口** | JDK 的 `Executor`、`ExecutorService` | Spring 的 `TaskExecutor`、`AsyncListenableTaskExecutor` |
| **@Async 支持** | 需要额外配置适配器 | 天然支持，直接使用 |

### **2. 功能扩展能力**

**ThreadPoolTaskExecutor 的优势：**
- 提供了更多可重写的方法（如 `execute`、`submit`、`submitListenable` 等）
- 方便统一添加 MDC 上下文传递（日志追踪必备功能）
- 可以方便地添加统一的监控日志
- 提供 `getThreadPoolExecutor()` 方法获取底层的原生线程池进行细致监控
- 方法级别的细粒度控制更容易实现

**ThreadPoolExecutor 的扩展：**
- 只能通过继承重写有限的方法
- 需要手动实现 MDC 传递等高级功能
- 监控逻辑需要自行设计和实现

**真正的区别在于：**
- `ThreadPoolTaskExecutor` 是 Spring 原生提供的类型，在 Spring 生态中使用更自然
- 与 Spring 的其他功能（如 `@Async`）配合时，使用 `ThreadPoolTaskExecutor` 更直观
- `ThreadPoolExecutor` 虽然也能注入，但在某些 Spring 特定场景下需要额外适配

---

## **四、适用场景建议**

**使用 ThreadPoolTaskExecutor 的场景：**
- Spring / Spring Boot 项目（强烈推荐）
- 需要使用 `@Async` 异步注解
- 需要统一的 MDC 日志追踪
- 需要与 Spring 生命周期深度整合
- 企业级应用，需要监控和管理

---

## **五、核心关系**

**层级结构：**
```
ThreadPoolTaskExecutor（Spring 封装层）
    ↓ 内部持有
ThreadPoolExecutor（JDK 底层实现）
    ↓ 管理
Thread（实际工作线程）
```


**关键理解：**
- `ThreadPoolTaskExecutor` 内部通过 `getThreadPoolExecutor()` 方法暴露底层的 `ThreadPoolExecutor`
- 前者是后者的**包装器（Wrapper）**，增加了 Spring 生态的便利性
- 学习时应先掌握 `ThreadPoolExecutor` 的原理，再理解 Spring 的封装价值

---

## **六、总结对比表**

| 维度 | ThreadPoolExecutor | ThreadPoolTaskExecutor |
|------|-------------------|----------------------|
| **来源** | JDK `java.util.concurrent` | Spring Framework |
| **配置方式** | 构造函数参数（7 个参数一次性传入） | Setter 方法 + 必须调用 initialize() |
| **@Async 配合** | 需要适配 | 天然支持 |
| **扩展方式** | 继承重写（方法有限） | 继承重写（提供更多钩子方法） |
| **MDC 支持** | 需手动实现 | 便于统一封装 |
| **监控能力** | 需自行设计实现 | 提供便捷方法和钩子 |

---

## **七、学习建议**

1. **先精通** `ThreadPoolExecutor` 的 7 个参数含义、工作流程、拒绝策略
2. **再理解** `ThreadPoolTaskExecutor` 如何简化 Spring 开发
3. **实际工作**中，Spring Boot 项目优先使用 `ThreadPoolTaskExecutor`
4. **面试准备**时，两个都要了解，重点掌握 `ThreadPoolExecutor` 的原理
---

# ThreadPoolTaskExecutor 中 execute 和 submit 的区别

## 方法来源不同

- **execute()**：定义在 `Executor` 接口中（最顶层接口）
- **submit()**：定义在 `ExecutorService` 接口中（Executor 的子接口）

## 参数类型不同

| 方法 | 可接受的参数类型 |
|------|----------------|
| execute() | 只接受 `Runnable` |
| submit() | 接受 `Runnable` 和 `Callable` |

## 返回值不同

| 方法 | 返回值 | 说明 |
|------|--------|------|
| execute() | `void`（无返回值） | 无法获取任务执行结果 |
| submit() | `Future<?>` | 可以通过 Future 获取任务结果或异常 |

## 异常处理不同

| 方法 | 异常处理方式 |
|------|-------------|
| execute() | 任务中的异常会直接抛出，可能导致线程终止 |
| submit() | 任务中的异常被封装在 Future 中，调用 get() 时才会抛出 ExecutionException |

## 使用场景对比

| 特性 | execute() | submit() |
|------|-----------|----------|
| 需要返回结果 |  不支持 | ✅ 支持 |
| 需要捕获异常 | ❌ 不方便 | ✅ 通过 Future.get() 捕获 |
| 性能开销 | 较小（无 Future 对象创建） | 稍大（需创建 Future 对象） |
| 适用场景 | 不需要结果的简单任务 | 需要结果或异常处理的任务 |

## 核心总结

- **execute()**：适合"发了就不管"的任务，不关心结果和异常
- **submit()**：适合需要获取结果、捕获异常或控制任务生命周期的场景

---

# FutureTask 如何获取结果，是否会阻塞主线程

## 获取结果的方式

通过调用 `FutureTask.get()` 方法获取结果：

```java
FutureTask<Integer> ft = new FutureTask<>(callable);
Thread t = new Thread(ft);
ft.start();

Integer result = ft.get();  // 获取结果
```


## 是否会阻塞主线程？

**答案：是的，会阻塞！**

`ft.get()` 是一个**阻塞方法**，具体行为如下：

### 阻塞的条件

| 任务状态 | get() 的行为 |
|---------|-------------|
| 任务已完成 | 立即返回结果，不阻塞 |
| 任务未完成 | 阻塞当前线程，直到任务完成 |

### 阻塞的原理

1. **主线程调用 get()** → 检查任务状态
2. **如果任务未完成** → 调用 `LockSupport.park()` 让主线程进入 WAITING 状态（挂起，不占用 CPU）
3. **工作线程完成任务** → 设置结果，调用 `LockSupport.unpark()` 唤醒主线程
4. **主线程被唤醒** → 从 park() 返回，获取结果并继续执行

### 阻塞的特点

| 特性 | 说明 |
|------|------|
| 是否占用 CPU | 否（WAITING 状态不占用 CPU） |
| 是否可以中断 | 是（调用 interrupt() 可抛出 InterruptedException） |
| 是否可以超时 | 是（使用 `get(timeout, TimeUnit)` 设置超时时间） |
| 阻塞哪个线程 | 调用 get() 的那个线程（通常是主线程） |

### 避免长时间阻塞的方法

#### 方法1：带超时的 get

```java
try {
    Integer result = ft.get(5, TimeUnit.SECONDS);  // 最多等 5 秒
} catch (TimeoutException e) {
    System.out.println("任务超时");
    ft.cancel(true);  // 取消任务
}
```


#### 方法2：先做其他事，最后再 get

```java
// 提交多个任务
FutureTask<Integer> ft1 = new FutureTask<>(task1);
FutureTask<Integer> ft2 = new FutureTask<>(task2);
new Thread(ft1).start();
new Thread(ft2).start();

// 先做其他事情
doOtherWork();

// 真正需要结果时才调用 get（此时可能任务已经完成，不会阻塞）
Integer result1 = ft1.get();
Integer result2 = ft2.get();
```


#### 方法3：检查任务是否完成

```java
if (ft.isDone()) {
    // 任务已完成，get() 不会阻塞
    Integer result = ft.get();
} else {
    // 任务未完成，可以选择等待或做其他事
}
```


### 核心总结

| 要点 | 说明 |
|------|------|
| **get() 是否阻塞** | 是，任务未完成时会阻塞调用线程 |
| **阻塞的本质** | 通过 LockSupport.park() 让线程进入 WAITING 状态 |
| **何时解除阻塞** | 任务完成时通过 LockSupport.unpark() 唤醒 |
| **是否一直阻塞** | 否，可以使用超时或 isDone() 检查来避免 |
| **最佳实践** | 提交任务后先做其他事，真正需要结果时再调用 get() |

---

我来为你解释这三个概念：

---

# CompletableFuture 是什么

### 基本概念

`CompletableFuture` 是 Java 8 引入的异步编程工具类，位于 `java.util.concurrent` 包中。它实现了 `Future` 和 `CompletionStage` 两个接口。

### 核心特性

| 特性 | 说明 |
|------|------|
| **异步非阻塞** | 任务异步执行，不阻塞主线程 |
| **函数式编程** | 支持链式调用，类似 Stream API |
| **结果回调** | 任务完成后自动触发回调，无需手动 get() |
| **异常处理** | 提供完善的异常处理机制 |
| **任务组合** | 可以轻松组合多个异步任务（串行、并行） |

### 与传统 Future 的对比

| 特性 | Future | CompletableFuture |
|------|--------|-------------------|
| 获取结果方式 | 必须调用 get()（阻塞） | 可以回调或 get()（非阻塞优先） |
| 是否阻塞 | get() 会阻塞 | 默认非阻塞，通过回调处理结果 |
| 任务编排 | 不支持 | 支持 thenApply、thenCompose、allOf 等 |
| 异常处理 | 需要在 get() 时捕获 | 提供 exceptionally、handle 等方法 |
| 手动完成 | 不支持 | 可以手动 complete() 设置结果 |

### 基本使用模式

```
创建异步任务 → 链式处理结果 → 异常处理 → 最终消费
```


### 典型应用场景

- 多个独立任务并行执行后合并结果
- 任务之间有依赖关系，需要串行执行
- 需要超时控制的任务
- 需要优雅处理异常的场景

---

# ListenableFuture 是什么

### 基本概念

`ListenableFuture` 是 **Spring Framework** 提供的接口，位于 `org.springframework.util.concurrent` 包中。它扩展了标准的 `Future` 接口。

### 核心特性

| 特性 | 说明 |
|------|------|
| **添加监听器** | 可以注册回调函数，任务完成时自动触发 |
| **非阻塞** | 不需要调用 get() 等待，通过回调获取结果 |
| **Spring 生态** | Spring 框架中的异步任务返回类型 |
| **兼容 Future** | 继承了 Future 的所有方法 |

### 与标准 Future 的对比

| 特性 | Future | ListenableFuture |
|------|--------|------------------|
| 获取结果 | 只能 get()（阻塞） | 可以 get() 或 addCallback()（非阻塞） |
| 回调支持 | ❌ 不支持 | ✅ 支持 SuccessCallback 和 FailureCallback |
| 所属框架 | JDK 标准库 | Spring Framework |
| 使用场景 | 通用异步任务 | Spring 异步方法返回值 |

### 核心方法

- `addCallback(SuccessCallback, FailureCallback)`：添加成功和失败回调
- `addCallback(ListenableFutureCallback)`：添加统一回调接口

---

# submitListenable 是什么

### 基本概念

`submitListenable()` 是 **Spring 的 ThreadPoolTaskExecutor** 提供的方法，用于提交任务并返回 `ListenableFuture`。

### 方法签名

```java
ListenableFuture<?> submitListenable(Runnable task)
ListenableFuture<T> submitListenable(Callable<T> task)
```


### 与普通 submit() 的对比

| 特性 | submit() | submitListenable() |
|------|----------|-------------------|
| 返回类型 | `Future<?>` | `ListenableFuture<?>` |
| 回调支持 | ❌ 不支持直接回调 | ✅ 支持 addCallback() |
| 所属类 | ExecutorService | ThreadPoolTaskExecutor（Spring） |
| 使用场景 | 标准 JDK 线程池 | Spring 管理的线程池 |

### 使用流程

```
提交任务 → 返回 ListenableFuture → 添加回调 → 任务完成自动触发回调
```


---

## 三者关系总结

### 继承和实现关系

```
JDK 标准库：
├── Future (接口)
│   └── CompletableFuture (实现类，Java 8+)

Spring Framework：
├── Future (接口，继承自 JDK)
│   └── ListenableFuture (接口，Spring 扩展)
│       └── CompletableFuture (也实现了这个接口)
```


### 功能对比表

| 特性 | Future | ListenableFuture | CompletableFuture |
|------|--------|------------------|-------------------|
| **来源** | JDK 1.5+ | Spring Framework | JDK 1.8+ |
| **阻塞获取** | ✅ get() | ✅ get() | ✅ get()（但不推荐） |
| **回调机制** |  不支持 | ✅ addCallback() | ✅ thenApply/thenAccept 等 |
| **链式编排** | ❌ 不支持 | ❌ 不支持 | ✅ 强大的链式 API |
| **异常处理** | 需在 get() 时捕获 | 通过 FailureCallback | exceptionally/handle |
| **手动完成** | ❌ 不支持 |  不支持 | ✅ complete()/completeExceptionally() |
| **任务组合** | ❌ 不支持 | ❌ 不支持 | ✅ allOf/anyOf/thenCombine 等 |
| **推荐使用度** | ⭐（传统方式） | ⭐⭐⭐（Spring 项目） | ⭐⭐⭐⭐⭐（现代推荐） |

### 演进关系

```
Future (JDK 1.5)
    ↓ 缺点：只能阻塞获取，无回调
    ↓
ListenableFuture (Spring)
    ↓ 改进：增加回调支持，但仍需手动管理
    ↓
CompletableFuture (JDK 1.8)
    ↓ 最佳：回调 + 链式编排 + 异常处理 + 任务组合
```


---

## 实际使用建议

### 场景1：纯 JDK 项目，简单异步任务

使用 `CompletableFuture`：

```
优势：无需额外依赖，功能强大，代码简洁
```


### 场景2：Spring 项目，需要与 Spring 集成

使用 `submitListenable()` 返回 `ListenableFuture`：

```
优势：与 Spring 异步注解 @Async 配合良好，便于统一管理
```


### 场景3：复杂异步流程编排

使用 `CompletableFuture`：

```
优势：链式调用清晰，异常处理好，支持并行和串行组合
```


### 核心选择原则

| 项目类型 | 推荐方案 | 原因 |
|---------|---------|------|
| 新项目（JDK 8+） | CompletableFuture | 功能最全，标准化 |
| Spring 老项目 | ListenableFuture | 与现有代码兼容 |
| 简单异步任务 | CompletableFuture 或 ListenableFuture | 看项目依赖 |
| 复杂异步编排 | CompletableFuture | 链式 API 更强大 |

---

# MDC(Mapped Diagnostic Context) 

MDC 是 SLF4J 提供的一个工具类，用于在多线程环境下存储和传递诊断上下文信息。它本质上是一个线程级别的 Map，可以存储键值对数据（如 traceId、userId 等），这些数据显示在日志中，方便追踪和调试。

**作用：** 日志中输出额外信息

MDC 是基于 ThreadLocal 实现的，子线程不会自动继承父线程的 MDC 数据！

**线程池场景：**
- 线程池中的线程会被复用
- 如果不清理 MDC，上一个任务的上下文可能污染下一个任务
- 所以 ThreadMdcUtil 在 finally 块中调用了 MDC.clear()

使用 MDC + ThreadMdcUtil 的原因就是：线程池执行任务时是不同的线程，它们需要拿到主线程的上下文信息（如 traceId、userId），这样才能保证日志的完整性和可追踪性。

```java
@Override
public void execute(Runnable task) {
// 1. 获取主线程的 MDC 上下文
Map<String, String> contextMap = MDC.getCopyOfContextMap();

    // 2. 包装任务，把上下文传递给子线程
    super.execute(ThreadMdcUtil.wrap(task, contextMap));
}
```

