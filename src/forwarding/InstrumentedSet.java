package forwarding;

import java.util.Collection;
import java.util.Set;

/**
 * 虽然与之前的 InstrumentedHashSet 非常类似，但应注意，这里的代码被分成了两块儿，
 * 一块儿是对它自己成员变量addCount的操作，另一块儿是对可重用的转发类进行调用
 * (这里的转发类是指ForwardingSet)，从而实现对需扩展对象的操作。
 *
 * @author LightDance
 */
public class InstrumentedSet<E> extends ForwardingSet<E> {

    private int addCount = 0;

    public int getAddCount() {
        return addCount;
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

    public InstrumentedSet(Set<E> s) {
        super(s);
    }
}
