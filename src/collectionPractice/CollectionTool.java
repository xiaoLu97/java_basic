package collectionPractice;

import java.util.ArrayList;
import java.util.Collections;

public class CollectionTool {
    // Collections 工具类专门用来操作集合的，添加元素、对元素进行排序、替换元素

    public static void main(String[] args) {
        TestAddAll();
    }
    public static void TestAddAll() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("A");
        Collections.addAll(list, "B", "C", "D"); // 动态参数
        System.out.println(list);
    }

}
