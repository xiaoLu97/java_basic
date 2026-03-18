package singleton;

public class Account {
    private Account() {
        System.out.println("创建Account对象");
    }
    private static Account account;

    // 同步指线程的状态，多个线程排队执行，加锁是实现同步的方法
    public synchronized static Account getInstance() {
        if (account == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            account = new Account();
        }
        return account;
    }
}
