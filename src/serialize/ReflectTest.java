package serialize;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectTest {
    public static void main(String[] args) throws Exception {
//        useMethod();
        useField();
    }
     public static void useField() throws Exception {
        Class clazz = User.class;
//        User user = new User(1, "张三");
        Constructor<User> constructor = clazz.getConstructor(Integer.class, String.class);
        User user = constructor.newInstance(1, "张三");
        Field common = clazz.getDeclaredField("common");
        common.set(user, "公共字段"); // 不能私有
        System.out.println(common.get(user));
    }
    public static void useMethod() throws Exception {
        Class cls = User.class;
        User user = new User(1, "张三");
        // 常规调用
        user.test(); // 操作对象
        // 反射调用
        Method test = cls.getMethod("test", null);
        test.invoke(user, null); // 操作方法
        Method test1 = cls.getDeclaredMethod("test", int.class);
        test1.invoke(user, 1); // public才能调用
    }
}
