package collectionPractice;

import java.util.*;

public class Main {
    public static void main(String[] args) {
//        listTest();
//        linkedListTest();
//        linkHashSetTest();
        treeSetTest();
    }

    public static void listTest() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("Hello");
        list.add("World");
        list.add("JavaEE");
        list.add("JavaSE");
        Iterator<String> it = list.iterator();
        while(it.hasNext()) {
            Object next = it.next();
            System.out.println(next);
        }
    }
    public static void linkedListTest() {
        LinkedList<String> list = new LinkedList<String>();
        list.add("Hello");
        list.offer("World");
        list.push("JavaEE");
        list.addFirst("JavaSE");
        System.out.println( list);

    }
    public static void linkHashSetTest() {
        LinkedHashSet<A> set = new LinkedHashSet<A>();
        set.add(new A(1));
        set.add(new A(2));
        set.add(new A(1));
        Iterator<A> it = set.iterator();
        while(it.hasNext()) {
            A next = it.next();
            System.out.println(next);
        }
    }

    public static void treeSetTest() {
        TreeSet<A> set = new TreeSet<A>();
        set.add(new A(4));
        set.add(new A(2));
        set.add(new A(3));
        set.add(new A(5));
        set.add(new A(6));
        set.add(new A(1));
        Iterator<A> it = set.iterator();
        while(it.hasNext()) {
            A next = it.next();
            System.out.println(next);
        }
    }

}

class A implements Comparable<A> {
    private int num;
    public A(int  num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "A {" + "num=" + num + '}';
    }

//    set怎么判断两个对象相等？
/*如果两个对象通过 equals() 判定为相等，那么它们的 hashCode() 必须返回相同的值
如果两个对象的 hashCode() 返回不同的值，那么它们的 equals() 必须判定为不相等*/
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        A a = (A) obj;
        return num == a.num;
    }

    @Override
    public int hashCode() {
        return Objects.hash(num);
    }

    /**
     * A.compareTo(B)
     * 1表示A大于B
     * 0表示A等于B
     * -1表示A小于B
     * @param a A
     * @return int
     */
    @Override
    public int compareTo(A a) {
        if (this.num > a.num) return 1;
        if (this.num < a.num) return -1;
        return 0;
    }
}
