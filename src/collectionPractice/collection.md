# 集合

可以简单理解为一个长度可以改变，可以保存任意数据类型的动态数组

在 Java 中，集合不是由一个类来完成的，而是由一组接口和类共同构成了一个框架体系，大致可分为 3 层，最上层是一组接口，继而是接口的实现类，接下来就是对集合各种操作的工具类

| 接口 | 描述 |
|------|------|
| Collection | 集合框架最基础的接口，一个 Collection 存储一组无序、不唯一的对象，一般不直接使用该接口 |
| List | Collection 的子接口，存储一组有序、不唯一的对象，开发中常用的接口之一 |
| Set | Collection 的子接口，存储一组无序、唯一的对象 |
| Map | 独立于 Collection 的另外一个接口，存储一组键值对象、提供键到值的映射 |
| Iterator | 专用用来输出集合元素的接口，一般适用于无序集合，从前向后单向输出元素 |
| ListIterator | Iterator 的子接口，可以双向输出集合中的元素 |
| Enumeration | 传统的输出接口，已经被 Iterator 所取代 |
| SortedSet | Set 的子接口，可以对集合中的元素进行排序 |
| SortedMap | Map 的子接口，可以对集合中的键值元素进行排序 |
| Queue | 队列接口，此接口的实现类可以实现队列操作 |
| Map.Entry | Map 的内部接口，描述 Map 中的一个键值对元素 |

```java
public interface Collection<E> extends Iterable<E> {}
// Iterable定义iterator方法
```

List接口的实现类:
- ArrayList
- LinkedList 采用链表的形式存储数据

Set接口的实现类:
- HashSet 无序
- LinkedHashSet 存储顺序和遍历顺序一致
- TreeSet 按元素升序进行排列,前提是可比较（java.lang.Comparable）

Map接口的实现类
- HashMap：存储一组无序、key 不可重复，value 可重复的元素 （线程不安全）
- Hashtable：存储一组无序、key 不可重复，value 可以重复的元素 (线程安全)
- TreeMap：存储一组有序、key 不可重复、value 可以重复的元素，可以按照 key 来排序

```java
Set set = hashMap.keySet();
Iterator iterator = set.iterator();
while (iterator.hasNext()) {
    System.out.println(iterator.next());
}

Collection values = hashMap.values();
Iterator iterator1 = values.iterator();
while (iterator1.hasNext()) {
    System.out.println(iterator1.next());
}

Set set1 = hashMap.entrySet();
Iterator iterator2 = set1.iterator();
while (iterator2.hasNext()) {
    System.out.println(iterator2.next());
}
```

# Collections 工具类
专门用来操作集合的，添加元素、对元素进行排序、替换元素

| 方法 | 描述 |
  |------|------|
| public static void sort(List list,Comparator) | 根据 Comparator 接口进行排序 |
| public static int binarySearch(List list,Object k) | 查找元素在集合中的下标，要求集合必须是升序排列 |
| public static Object get(int index) | 根据下标找到元素 |
| public static void reverse(List list) | 对集合元素的顺序进行反转 |
| public static void swap(List list,int i,int j) | 交换两个元素的位置 |
| public static void fill(List list,Object o) | 将集合中的元素全部替换为 o |
| public static void min(List list) | 返回集合中的最小值 |
| public static void max(List list) | 返回集合中的最大值 |
| public static boolean replaceAll(List list,Object oldV,Object newV) | 将集合中所有的 oldV 替换为 newV |
| public static boolean addAll(List list,Object... o) | 向集合中添加元素 |