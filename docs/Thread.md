# Java 多线程学习指南

> 基于项目代码和课程笔记整理的系统性复习文档

---

## 一、文件地图

### 课程笔记（按学习顺序）

| 顺序 | 文件 | 主题 | 关键词 |
|------|------|------|--------|
| ① | `2026版Java全栈课程/009.md`（后半段） | 进程与线程概念、多线程初体验 | 进程、线程、单核/多核CPU |
| ② | `2026版Java全栈课程/010.md` | 线程创建方式、状态、lambda、sleep | Thread、Runnable、5种状态、lambda简化 |
| ③ | `2026版Java全栈课程/011.md` | 线程合并、休眠、线程同步入门 | join、sleep、synchronized |
| ④ | `2026版Java全栈课程/012.md` | 单例安全、死锁、重入锁、生产者消费者 | ReentrantLock、死锁、wait/notify |
| ⑤ | `2026版Java全栈课程/013.md` | sleep vs wait、synchronized锁谁、并发修改异常 | wait/notify、锁对象、CopyOnWriteArrayList |
| ⑥ | `2026版Java全栈课程/014.md` | JUC工具包、线程池 | CountDownLatch、Semaphore、ReadWriteLock、ThreadPool |
| ⑦ | `2026版Java全栈课程/015.md` | ForkJoin框架、volatile | 递归拆分、内存可见性、JMM |

### 代码文件（对照练习）

| 对应阶段 | 文件 | 演示了什么 |
|----------|------|-----------|
| 基础创建 | `src/thread/MyThread1.java` | 继承 `Thread` 类 |
| 基础创建 | `src/thread/MyThread2.java` | 继承 `Thread` 类 |
| 基础创建 | `src/thread/Main.java` | 3种创建方式 + sleep + lambda |
| 线程同步 | `src/thread/Account.java` | `ReentrantLock` 手动加锁解锁 |
| 线程同步 | `src/thread/Test.java` | 多线程共享 Runnable 任务 |
| 线程同步 | `src/thread/SynKey.java` | `synchronized` 代码块 + 锁对象分析 |
| volatile | `src/singleton/VolatileDemo.java` | 工作内存不可见问题 |
| 线程池 | `src/executorTool/Test.java` | `Executors.newFixedThreadPool` |
| JUC工具 | `src/jucTool/Main.java` | CountDownLatch、Semaphore、ReadWriteLock、ForkJoin 全家桶 |

---

## 二、学习路线（6 个阶段）

### 阶段 1：线程是什么

> 对应：`009.md` 后半段

**核心概念：**

- **进程** = 正在运行的应用程序，拥有独立的内存空间
- **线程** = 进程内的执行单元，共享进程的内存空间
- 单核 CPU 下多线程是交替执行（速度快，看起来像同时）
- 多核 CPU 下多线程是真正并行执行

**多线程的优缺点：**

- 优点：资源利用更合理、程序响应更快、程序设计更简洁
- 缺点：需要更多内存、并发访问可能影响数据准确性、可能出现死锁

**动手跑：** `src/thread/Main.java` — 观察 3 个线程的输出是交替的，不是顺序的。

---

### 阶段 2：线程的创建与生命周期

> 对应：`010.md` + `011.md` 前半段
> 对应代码：`src/thread/Main.java`、`MyThread1.java`、`MyThread2.java`

**3 种创建方式：**

```java
// 方式1：继承 Thread 类（不推荐，耦合度高，Java 单继承限制）
class MyThread1 extends Thread {
    @Override
    public void run() {
        // 任务逻辑
    }
}
new MyThread1().start();

// 方式2：实现 Runnable 接口（解耦任务和线程，推荐使用）
class Account implements Runnable {
    @Override
    public void run() {
        // 任务逻辑
    }
}
new Thread(new Account()).start();

// 方式3：Lambda 简化（Runnable 只有一个方法时可用）
new Thread(() -> {
    for (int i = 1; i <= 50; i++) {
        System.out.println("Lambda: " + i);
    }
}).start();
```

**线程的 5 种状态：**

```
创建状态 ──start()──→ 就绪状态 ──获取CPU──→ 运行状态
                           ↑                   │
                           │                   ↓（时间到/让出）
                           └──────────── 阻塞状态（sleep/wait）
                                               │
                                               ↓（唤醒/时间到）
                                             就绪状态

运行状态 ──任务完成/异常──→ 终止状态
```

| 状态 | 说明 |
|------|------|
| 创建 | 实例化了线程对象，还未启动 |
| 就绪 | 调用了 start()，在线程池中等待 CPU 资源 |
| 运行 | 获取了 CPU 资源，正在执行任务 |
| 阻塞 | 暂停执行，释放 CPU 资源 |
| 终止 | 运行完毕或异常导致结束 |

**关键方法对比：**

| 方法 | 作用 | 注意点 |
|------|------|--------|
| `start()` | 启动线程，进入就绪状态 | 不能重复调用 |
| `run()` | 线程的任务逻辑 | 直接调用 = 普通方法调用，不会开新线程 |
| `sleep(ms)` | 让当前线程休眠 | 不释放锁，时间到了自动醒 |
| `join()` | 等待另一个线程执行完毕 | 把别的线程合并到自己的执行流中 |
| `join(ms)` | 最多等待指定毫秒 | 超时后两个线程重新争夺 CPU |

**线程休眠的关键理解：**
- sleep 在哪个线程中调用，就让哪个线程休眠，和调用者无关
- 在主线程调用 `Thread.sleep(3000)` → 主线程休眠
- 在子线程的 run 中调用 `Thread.sleep(3000)` → 子线程休眠

---

### 阶段 3：线程安全

> 对应：`011.md` 后半段 + `012.md` + `013.md`
> 对应代码：`src/thread/Account.java`、`src/thread/Test.java`、`src/thread/SynKey.java`

这是**最重要**的部分。

**为什么需要同步？**

```java
// src/thread/Account.java 中：
private static int num;  // 共享变量
num++;
// num++ 不是原子操作！
// 它实际上是：读取num → 加1 → 写回num
// 线程A读取num=5，线程B也读取num=5
// 线程A写回6，线程B也写回6（本应是7）→ 数据丢失
```

**同步和异步的区别：**
- 同步 = 多个线程排队执行（一个一个来）
- 异步 = 多个线程同时执行（并发）

**3 种加锁方式：**

```java
// 1. synchronized 修饰方法（自动加锁，自动解锁）
public synchronized void run() {
    num++;
    // 整个方法加锁，同一时间只有一个线程能执行
}

// 2. synchronized 修饰代码块（锁指定对象，粒度更细）
synchronized (this) {
    // 只有这段代码加锁
}

// 3. ReentrantLock（手动加锁，手动解锁，功能更强）
private ReentrantLock reentrantLock = new ReentrantLock();

public void run() {
    reentrantLock.lock();    // 手动加锁
    try {
        num++;
    } finally {
        reentrantLock.unlock(); // 必须在 finally 中解锁！
    }
}
```

**synchronized 锁的到底是谁？（核心考点）**

| 修饰什么 | 锁的对象 | 效果 |
|----------|----------|------|
| 实例方法 | `this`（方法的调用者） | 同一个对象的多个 synchronized 方法互斥 |
| 静态方法 | `Class` 对象 | 所有对象的该方法都互斥（全局只有一份） |
| 代码块 `synchronized(obj)` | `obj` 这个对象 | 看 obj 在内存中有几份 |

**核心判断规则：**
> 看被锁定的资源在内存中有几份。只有一份 → 多线程排队（同步）；多份 → 不排队（不同步）。

**实际例子分析：**

```java
// 场景1：共享同一个对象 → 同步
SynKey t = new SynKey();
// 5个线程都调用 t.test()，锁的是同一个 t 对象 → 排队执行

// 场景2：每个线程创建新对象 → 不同步
// 每个线程里 new SynKey()，锁的是不同的对象 → 不排队
```

**ReentrantLock vs synchronized：**

| | synchronized | ReentrantLock |
|---|---|---|
| 实现层面 | JVM 层面 | JDK 层面（API 调用） |
| 加锁方式 | 自动加锁 | 手动 lock() |
| 解锁方式 | 自动解锁 | 手动 unlock()（必须在 finally 中） |
| 重复上锁 | 不支持 | 支持（可重入） |
| 限时等待 | 不支持 | tryLock(timeout) 支持 |
| 判断锁状态 | 不支持 | isHeldByCurrentThread() |

**死锁（012.md）：**

两个线程互相持有对方需要的锁，谁都不放，形成永久等待。

```
线程A：持有锁1，等待锁2
线程B：持有锁2，等待锁1
→ 谁都无法继续，死锁
```

避免死锁的方法：错开时间启动线程，或者按相同顺序获取锁。

**sleep vs wait：**

| | sleep | wait |
|---|---|---|
| 所属类 | Thread 类 | Object 类 |
| 作用对象 | 当前线程本身 | 当前正在访问的资源对象 |
| 释放锁 | ❌ 不释放 | ✅ 释放 |
| 唤醒方式 | 时间到自动醒 | notify/notifyAll 唤醒，或指定时间 |
| 使用限制 | 任何地方都能调 | 必须在 synchronized 方法/代码块中 |
| 解除阻塞 | 时间到 | wait(millis) 时间到，或 notify/notifyAll |

**生产者消费者模式（012.md 经典案例）：**

```java
// 容器：生产者和消费者共享
public synchronized void push(物品) {
    while (容器满了) {
        this.wait();    // 容器满了，生产者等待，释放锁给消费者
    }
    this.notify();      // 放入物品后，唤醒消费者
    // 添加物品...
}

public synchronized 物品 pop() {
    while (容器空了) {
        this.wait();    // 容器空了，消费者等待，释放锁给生产者
    }
    this.notify();      // 取出物品后，唤醒生产者
    // 取出物品...
}
```

**ConcurrentModificationException 并发修改异常（013.md）：**

ArrayList 是线程不安全的，多线程并发操作会出问题。3 种解决方案：

```java
// 方案1：换成 Vector（全部方法加 synchronized，性能差）

// 方案2：Collections.synchronizedList 包装
List<String> list = Collections.synchronizedList(new ArrayList<>());

// 方案3：CopyOnWriteArrayList（JUC，写时复制，读不加锁，性能最好）
List<String> list = new CopyOnWriteArrayList<>();
```

CopyOnWrite 原理：写操作时复制一份新数组，在新数组上操作，操作完再将引用指向新数组。读操作不加锁，因为读写操作的是不同的数组。

---

### 阶段 4：volatile 关键字

> 对应：`015.md` 后半段
> 对应代码：`src/singleton/VolatileDemo.java`

**Java 内存模型（JMM）：**

```
┌──────────────────────────────────────────┐
│              主内存（共享）                │
│         int num = 0;                     │
└──────┬───────────────────────┬───────────┘
       │ 复制                  │ 复制
       ↓                       ↓
┌──────────────┐      ┌──────────────┐
│ 线程A 工作内存 │      │ 线程B 工作内存 │
│ num = 0（副本）│      │ num = 0（副本）│
└──────────────┘      └──────────────┘
```

**问题演示：**

```java
// src/singleton/VolatileDemo.java
public static int num = 0;

// 子线程：一直检查 num == 0
new Thread(() -> {
    while (num == 0) {
        // 死循环：工作内存中的 num 一直是旧的 0
    }
}).start();

// 主线程：1秒后修改 num = 1
num = 1;
// 结果：输出 "num = 1"，但子线程的循环永远不会停止！
// 原因：子线程的工作内存中 num 还是 0，看不到主内存的变化
```

**解决：加 volatile**

```java
public static volatile int num = 0;
// volatile 保证：每次读取都直接从主内存读，修改后立即写回主内存
// 即：保证内存可见性
```

**volatile 的局限性：**
- ✅ 保证可见性（多线程看到的数据一致）
- ❌ 不保证原子性（num++ 仍然不安全，需要 synchronized 或 AtomicInteger）

**volatile 在单例模式中的应用（双重检查锁）：**

```java
public class SingletonDemo {
    private volatile static SingletonDemo instance;  // 必须加 volatile
    public static SingletonDemo getInstance() {
        if (instance == null) {                    // 第一次检查（无锁，快速）
            synchronized (SingletonDemo.class) {   // 加锁
                if (instance == null) {             // 第二次检查（防止重复创建）
                    instance = new SingletonDemo();
                }
            }
        }
        return instance;
    }
}
```

为什么要加 volatile？因为 `instance = new SingletonDemo()` 实际上分 3 步：
1. 分配内存空间
2. 初始化对象
3. 将引用指向内存地址

没有 volatile 的话，步骤 2 和 3 可能被重排序，其他线程可能拿到未初始化的对象。

---

### 阶段 5：线程池

> 对应：`014.md` 后半段
> 对应代码：`src/executorTool/Test.java`

**池化思想：** 预先创建好资源，用完归还，不销毁。和字符串常量池、数据库连接池是同一个思想。

**3 种快捷创建方式：**

```java
// 1. 单例线程池：只有 1 个线程，保证任务顺序执行
ExecutorService pool = Executors.newSingleThreadExecutor();

// 2. 固定线程池：固定 N 个线程
ExecutorService pool = Executors.newFixedThreadPool(3);

// 3. 缓存线程池：按需创建，空闲 60 秒自动回收
ExecutorService pool = Executors.newCachedThreadPool();

// 提交任务
pool.execute(() -> {
    System.out.println(Thread.currentThread().getName() + " 执行任务");
});

// 关闭线程池（不再接收新任务，等待现有任务完成）
pool.shutdown();
```

**ThreadPoolExecutor 7 个核心参数：**

```java
new ThreadPoolExecutor(
    2,                              // corePoolSize：核心线程数（常驻）
    5,                              // maximumPoolSize：最大线程数
    1L,                             // keepAliveTime：空闲线程存活时间
    TimeUnit.SECONDS,               // unit：时间单位
    new ArrayBlockingQueue<>(3),    // workQueue：阻塞队列（等待区）
    Executors.defaultThreadFactory(),// threadFactory：线程工厂
    new ThreadPoolExecutor.AbortPolicy() // handler：拒绝策略
);
```

**线程池工作流程：**

```
新任务到来
    ↓
核心线程有空？ ──是──→ 分配给核心线程
    ↓ 否
队列未满？ ──是──→ 放入队列等待
    ↓ 否
未达最大线程？ ──是──→ 创建临时线程处理
    ↓ 否
执行拒绝策略（报错/丢弃/由调用线程执行...）
```

**4 种拒绝策略：**

| 策略 | 行为 |
|------|------|
| AbortPolicy（默认） | 抛出 RejectedExecutionException |
| CallerRunsPolicy | 由提交任务的线程自己执行 |
| DiscardPolicy | 直接丢弃任务 |
| DiscardOldestPolicy | 丢弃队列最老的任务 |

---

### 阶段 6：JUC 并发工具包

> 对应：`014.md` 前半段 + `015.md` 前半段
> 对应代码：`src/jucTool/Main.java`（精华，所有工具都有演示）

#### 6.1 CountDownLatch（减法计数器）

> 场景：确保某个线程等其他线程全部完成后再执行

```java
CountDownLatch latch = new CountDownLatch(50);  // 计数器初始值 50

// 子线程：执行任务，每次 countDown 减 1
new Thread(() -> {
    for (int i = 0; i < 50; i++) {
        // 做事...
        latch.countDown();  // 计数器 -1
    }
}).start();

// 主线程：await 阻塞等待，直到计数器归零
latch.await();
// 计数器到 0 后，这里才开始执行
```

#### 6.2 CyclicBarrier（加法计数器）

> 场景：凑够 N 个线程再一起放行，可重复使用

```java
CyclicBarrier barrier = new CyclicBarrier(10, () -> {
    System.out.println("凑满10个，放行！");
});

for (int i = 0; i < 30; i++) {
    new Thread(() -> {
        barrier.await();  // 到达屏障，等待其他线程
    }).start();
}
// 每凑满 10 个就放行一次，30 个线程放行 3 次
```

#### 6.3 Semaphore（计数信号量 / 限流）

> 场景：限制同时访问某个资源的线程数量

```java
Semaphore semaphore = new Semaphore(5);  // 最多允许 5 个线程同时访问

for (int i = 0; i < 15; i++) {
    new Thread(() -> {
        try {
            semaphore.acquire();    // 获取许可（没有就阻塞等待）
            System.out.println(Thread.currentThread().getName() + " 进入");
            Thread.sleep(2000);     // 模拟使用资源
            System.out.println(Thread.currentThread().getName() + " 离开");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();    // 释放许可（必须在 finally 中！）
        }
    }).start();
}
// 15 个线程，但同一时刻最多 5 个在执行
```

#### 6.4 ReadWriteLock（读写锁）

> 场景：读多写少，允许多线程同时读，但写操作独占

```java
class Cache {
    private final Map<Integer, String> map = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // 写操作：加写锁（独占，阻塞读和写）
    public void write(Integer key, String value) {
        lock.writeLock().lock();
        try {
            map.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 读操作：加读锁（共享，多个线程可同时读）
    public String read(Integer key) {
        lock.readLock().lock();
        try {
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
}
```

**读写锁规则：**
- 读-读：共享，不阻塞
- 读-写：互斥，写要等所有读完成
- 写-写：互斥，排队执行

#### 6.5 ForkJoin 框架

> 场景：将一个大任务拆分成多个小任务并行执行，最后合并结果

```java
// 1. 定义任务：继承 RecursiveTask（有返回值）或 RecursiveAction（无返回值）
class ForkJoinDemo extends RecursiveTask<Long> {
    private Long start, end;
    private Long threshold = 100_00000L;  // 拆分阈值

    public ForkJoinDemo(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        if ((end - start) < threshold) {
            // 小任务：直接计算
            Long sum = 0L;
            for (Long i = start; i <= end; i++) sum += i;
            return sum;
        } else {
            // 大任务：拆分
            Long mid = (start + end) / 2;
            ForkJoinDemo task1 = new ForkJoinDemo(start, mid);
            task1.fork();  // 异步执行子任务
            ForkJoinDemo task2 = new ForkJoinDemo(mid + 1, end);
            task2.fork();
            return task1.join() + task2.join();  // 等待并合并结果
        }
    }
}

// 2. 创建线程池并执行
ForkJoinPool pool = new ForkJoinPool();
ForkJoinTask<Long> task = new ForkJoinDemo(0L, 20_0000_0000L);
pool.execute(task);
Long result = task.get();  // 阻塞等待最终结果
```

---

## 三、核心知识点速查表

### 必须掌握的 3 个核心问题

| # | 问题 | 答案 |
|---|------|------|
| 1 | 为什么需要线程安全？ | 共享变量 + 非原子操作 → 数据不一致 |
| 2 | synchronized 锁的是谁？ | 实例方法锁 this，静态方法锁 Class，代码块锁传入的对象 |
| 3 | volatile 解决什么？ | 工作内存副本不同步的问题（可见性），不解决原子性 |

### 关键对比汇总

| 对比项 | A | B |
|--------|---|---|
| 线程创建 | 继承 Thread（耦合） | 实现 Runnable（解耦，推荐） |
| 休眠 | sleep（不释放锁） | wait（释放锁，需 notify 唤醒） |
| 加锁 | synchronized（自动） | ReentrantLock（手动，功能更强） |
| 计数器 | CountDownLatch（减法，一次性） | CyclicBarrier（加法，可重复） |
| 集合安全 | ArrayList（不安全） | CopyOnWriteArrayList（写时复制，安全） |

---

## 四、建议学习计划

| 天数 | 阅读内容 | 对应代码 | 学习目标 |
|------|----------|----------|----------|
| 第1天 | 009.md 后半段 + 010.md | `thread/Main.java` | 理解进程/线程，会创建和启动线程 |
| 第2天 | 011.md | 实验 join、sleep | 掌握线程合并和休眠 |
| 第3天 | 012.md | `thread/Test.java`、`SynKey.java` | 理解 synchronized 和 ReentrantLock |
| 第4天 | 013.md | 分析 SynKey 锁的是谁 | 搞懂 wait/notify、锁对象判断规则 |
| 第5天 | 015.md 后半段 | `singleton/VolatileDemo.java` | 理解 JMM 和 volatile 可见性 |
| 第6天 | 014.md | `executorTool/Test.java` | 掌握线程池创建和核心参数 |
| 第7天 | 014.md + 015.md | `jucTool/Main.java` | 掌握 JUC 五大工具 |
