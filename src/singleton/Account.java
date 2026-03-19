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
