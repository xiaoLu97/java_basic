package serialize;

/*
  反射
    通过一个实例化对象映射到类，在程序运行期间就可以获取类的信息，进行相关操作。
  Class 类
    Class 类是反射的基础
    用一个对象来表示某个类的信息，通过 Class 类来创建
    Class 是专门用来描述其他类的类，每一个 Class 对象都是对某个类的具体描述
    1、调用 forName 方法
    2、通过目标类的类字面量获取
    3、通过目标类的实例化对象获取
*/
public class ReflectDemo {
    public static void main(String[] args) throws Exception {
        getClassMethod();
    }

    public static void getClassMethod() throws Exception {
        //forName
        Class clazz1 = Class.forName("serialize.User");

        //类字面量
        Class clazz2 = User.class;

        //实例化对象
        User user = new User(1, "张三");
        Class clazz3 = user.getClass();

        System.out.println(clazz1 == clazz2);
        System.out.println(clazz2 == clazz3);
        System.out.println(clazz1); // 3个是同一个Class对象，用来获取类的结构
    }
}
