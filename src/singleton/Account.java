package singleton;

public class Account {
    private Account() {
        System.out.println("创建Account对象");
    }

    // 同步指线程的状态，多个线程排队执行，加锁是实现同步的方法
/*
    private static Account account;
    public synchronized static Account getInstance() {
        if (account == null) {
            account = new Account();
        }
        return account;
    }
*/

/*
    为什么要加volatile关键字？
    背景： 主内存先复制一份数据到工作内存，线程对工作内存的数据进行操作，操作完成之后再将数据保存到主内存中，这个过程就会存在线程安全问题。
    具体地，线程在工作内存操作完成后释放锁，但数据还没有保存到主内存中，其他线程访问时，复制到工作内存的数据还是null。

    volatile 关键字的作用是可以使内存中的数据对线程可见
    Java 内存模型 JMM Java Memory Model
    一个线程在访问内存数据的时候，其实不是拿到数据本身，而是将数据复制保存到工作内存中，相当于使用的是一个副本，对工作内存中的数据进行修改，修改完成之后再保存到主内存中，主内存对线程不可见。

    方法：volatile让线程直接访问主内存中的数据，而不是工作内存中的数据
*/
    private static volatile Account account;
    public static Account getInstance() {
        if (account == null) {
            synchronized(Account.class) {
                if (account == null) {
                    account = new Account();
                }
            }
        }
        return account;
    }
}
