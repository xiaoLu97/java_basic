集合可以简单理解为一个长度可以改变，可以保存任意数据类型的动态数组

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