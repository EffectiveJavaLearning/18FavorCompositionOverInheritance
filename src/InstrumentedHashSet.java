import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * 为了实现“查询HashSet自创建以来添加过多少个元素”(与{@link HashSet#size()}有区别，是每次创建的记录总数，
 * 包含了被remove掉的记录)。于是编写了一个HashSet的子类，尝试对插入元素的次数进行计数，
 * 并提供访问这一字段的方法。HashSet类有两个涉及元素添加的方法add()和addAll()，因此我们重写了这两个方法。
 * {@link #add(Object)},{@link #addAll(Collection)}
 *
 * 虽然这么干看上去挺合理的，但其实并不。比如当我们创建一个这样的实例然后用addAll()添三个元素进去时,
 * addCount的值竟然是6?!{@link #main}
 *      注：这里用来创建List时用的是Array.asList(),而Java9中可以用List.of()创建。
 *
 * 这是因为，{@link HashSet#addAll(Collection)}方法是在{@link HashSet#add(Object)}方法的基础上设计的，
 * 每次会调用add()方法向其中添加元素，因此打印出了错误的结果。虽然放弃重写addAll()方法可以修正这个问题，
 * 但应当注意到，addAll()调用add()是其方法的实现细节，这些细节在之后的版本中是随时有可能会发生改变的，
 * 因而这个InstrumentedHashSet类非常容易出现问题。
 *
 * 或许完全重写addAll()方法，自己实现对add()的调用可以解决这个问题，但这种方法也不总是行得通的，
 * 一则这相当于重新实现父类的方法，比较麻烦；二则这些父类的方法可能会涉及到其private型变量，
 * 这种变量在子类中是无法访问的，这种情况下无法通过该方案解决问题。
 *
 * 如果感觉上面的问题都是由于覆盖引起的，而通过添加新方法的方式继承并扩展父类就安全，那也未必。
 * 虽然这种方式确实安全得多，但也有特例：如果你在子类中添加了一个方法，然后不巧，
 * 某次更新时父类添加了一个同名的方法，但返回类型不同，那么子类就无法通过编译；而万一返回类型也相同，
 * 那么又回到了覆盖父类方法的那种情况，而且这种情况下，自己添加的方法是否能够遵守父类的规约也同样值得怀疑，
 * 因为给子类写这个方法的时候父类的同名方法还没出现。
 *
 * @author LightDance
 */
public class InstrumentedHashSet<E> extends HashSet<E> {
    private int addCount = 0;

    public int getAddCount() {
        return addCount;
    }

    public InstrumentedHashSet() {}
    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public static void main(String[] args) {
        InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
        s.addAll(Arrays.asList("Snap", "Crakle" , "Pop"));
        //result is 6
        System.out.println(s.getAddCount());
    }
}