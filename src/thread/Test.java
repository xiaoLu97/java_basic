package thread;

public class Test {
    public static void main(String[] args) {
        Account account = new Account();
        Thread thread1 = new Thread(account, "张三");
        Thread thread2 = new Thread(account, "李四");
        thread1.start();
        thread2.start();
    }
}