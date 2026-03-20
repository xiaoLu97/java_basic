package singleton;

import java.util.concurrent.TimeUnit;

public class VolatileDemo {
    public static int num = 0;
    public static void main(String[] args) {
        new Thread(()->{
            while (num == 0){
                // 不加volatile，一直是工作内存旧的num
            }
        }).start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        num = 1;
        System.out.println("num = " + num);
    }
}
