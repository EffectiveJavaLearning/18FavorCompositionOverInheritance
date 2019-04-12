import java.util.Properties;

/**
 * 复合应当优先于继承。
 * 继承是代码复用的重要途经之一，但并非总是最佳选择。如果使用不当，会导致代码鲁棒性降低。
 * 在package内部使用继承一般是安全的，因为子类父类都由同一程序员设计管理；对于专门设计为用来继承的、
 * 提供良好文档的类来说，继承也很安全；但普通类的跨package继承则是非常危险的。
 *
 * 注：这里的继承是指extends这种类与类之间的继承，而不涉及implements这种接口间或接口与类间的继承。
 *
 * 与方法调用不同，继承违反了封装特性。即是说，子类依赖父类中特定功能的实现细节，以此实现子类中的某些功能，
 * 如果父类中对应的实现细节随版本发生变化，那么子类就会被破坏。因此，子类应当随着父类一起变化，
 * 除非父类是专门为被继承而设计的，并提供了详细无误的文档说明。
 *
 * 为了更好地理解，请看这个继承HashSet类的例子{@link InstrumentedHashSet}
 *
 * 不过有一种方式可以完美解决这些问题，那就是用复合代替对父类的继承：在新编写的class中，
 * 保存一个对需要扩展的类的private型引用。这种方式被称为“复合”(composition)。
 * 由于要扩展的类变为了新类的一个成员变量，因此新类中的方法都可以调用要扩展类的对应方法，
 * 并返回其结果。这也被称为“转发”(forwarding)，这种方法也被叫做“转发方法”(forwarding method)。
 * 因为不再依赖其他类的内部实现细节，这种类会非常稳定，即使版本更新时向private型实例的类中添加新方法，
 * 也不会对该类产生影响。具体细节请看这个例子：{@link forwarding.InstrumentedSet}.
 * 注意这个例子中的继承被分成了两块儿，一个是它本身，另一个是可重用的转发类(转发类中仅包含各种转发方法)
 *
 * 由于Set接口的存在，只要在构造方法中将HashSet这种实现Set的子类传入，就可以添加计数功能。除鲁棒性外，
 * 这种方式的灵活性也值得拿来吹一吹。这种方式对作为构造方法参数的Set子类实例来说，实现了插拔特性，
 * 而且可以兼容任意Set的子类，甚至对已经投入使用的对象添加计数功能。比如：
 *      Set<Instant> times = new InstrumentedSet<>(new TreeSet<>(cmp));
 *      Set<E> s = new InstrumentedSet<>(new HashSet<>(INIT_CAPACITY));
 *      static void walk(Set<Dog> dogs) {
 *          InstrumentedSet<Dog> iDogs = new InstrumentedSet<>(dogs);
 *          //...
 *      }
 * 而使用继承的话就只能适用于单个、非接口的类，且需要对父类中每个含参构造函数提供单独的对应构造函数。
 *
 * {@link forwarding.InstrumentedSet}被称为包装类(wrapper class),是因为它的每个实例都把另一个Set
 * 类型实例包装了起来。这也称“装饰器模式”(decorator pattern)，因为计数功能是通过InstrumentedSet
 * 添加额外的“装饰品”实现的。
 *
 * 有时，复合+转发的结合会被错误地称为“委托”(delegation)，但在技术层面上讲，这并不叫委托，
 * 因为包装类(例如这里的InstrumentedSet)并没有把自己实例的引用放在被包装类那里(例如这里的Set)。
 *
 * 包装类的缺点不多，但很重要的一个是不要在回调框架中使用它。由于被包装类并不知道包装类把自己给包装了，
 * 且不持有对被包装类的引用，因此它使用回调时，最后回调触发会避开外部的包装对象，从而引发“SELF”问题。
 *
 * 关于性能和内存占用，这种方式并不会对性能造成多大的影响，编写转发方法可能会比较繁琐，比如
 * {@link forwarding.ForwardingSet}，但这种工作仅仅需要做一次，就可以通过接口去与想要的类型复合使用。
 * 例如Guava(一种开源的，为了方便编码、减少代码中错误而编写的Java库，也是谷歌的常用核心库)中，
 * 就为所有的集合接口提供了转发类。
 *
 * 继承仅在父类子类存在is-a关系时才适用，也就是说，如果想用类B扩展一个类A，首先要问自己：
 * 所有B都"is a"A吗？如果不能完全肯定，那么最好别用。
 * 答案为否定的情况一般是由于B需要包含A的私有实例，并提供不同的public型API了。
 * 这就表明，A不是B的一个基本部分，只是其实现的一个细节。
 *
 * 不幸Java类库中有很多地方都没遵守这条，比如{@link java.util.Stack}并不"is a"
 * {@link java.util.Vector}，因此本不应该扩展它；{@link java.util.Properties}也不应该扩展
 * {@link java.util.Hashtable}。这两组例子中，用复合代替继承效果会更好。
 *
 * 如果在该用复用的地方用了继承，那么就会不必要地暴露实现细节。这样的API就会被原始实现所限制，
 * 难以提升性能；更严重的是，这样还会将内部信息公开给客户端，让客户端能直接访问这些被暴露的字段。
 * 就算不出问题，也会导致语义混乱不清。例如{@link Properties}继承了{@link java.util.Hashtable},
 * 然后{@link Properties#getProperty(String)}跟继承自父类的{@link Properties#get(Object)}
 * 就容易被弄混；但前者考虑了默认的属性表，而后者则是直接继承了hashSet，并未考虑默认值，
 * 因而两种方法返回结果可能会不一致。{@link #inconsistencyCausedByDefaultValue()}
 *
 * 更严重的是，{@link Properties}在设计时仅允许设置字符串型value，而{@link java.util.Hashtable}
 * 则没有这样的限制。那么就有可能直接通过父类向子类设置非法字段，这样的情况一旦发生，
 * 那么子类的功能就会严重受损，比如无法使用API中的load()和store()，但又难以修复，
 * 因为客户端已经有key-value对依赖上非String型字段了。比如这个{@link #destroyBySuperClass()}
 * 就会抛出异常。
 *
 * 还有最后一个要考虑的方面：你要继承的类中，API是否存在缺陷？如果是的话，
 * 那么真的打算把这个缺陷通过继承传播出去吗？继承会将父类的缺陷传递给子类，
 * 但使用复合就允许我们在新的class中设计屏蔽或消除这些缺陷的方法，然后应用到新的API中。
 *
 * 不可否认，继承是一个非常强大的功能，但它违反了封装(encapsulation)原则。仅有十分确认
 * 父类子类间存在"is a"关系时，才可以使用。但即使如此，父类子类若处在不同package中，
 * 或者父类并不是专门为继承而设计的，或者没有提供良好的文档，这些都会令子类非常容易出问题。
 * 因此，必要时用复合+转发代替继承，尤其当存在接口能够实现包装类的时候，
 * 这种方式灵活性和鲁棒性都比继承高一些。
 *
 * @author LightDance
 */
public class FavorCompositionOverInheritance {
    /**
     * Properties继承了HashSet类，但Properties.getProperties中有对默认配置表
     * defP 的判断条件语句，而其父类中则没有，于是导致了两个get所得结果不一致。
     */
    public static void inconsistencyCausedByDefaultValue() {

        Properties defP = new Properties();
        defP.setProperty("key" , "100");
        Properties p = new Properties(defP);
        //null
        System.out.println(p.get("key"));
        //100
        System.out.println(p.getProperty("key"));
    }

    public static void destroyBySuperClass(){
        Properties p = new Properties();
        Object keyObj = new Object();
        p.put(keyObj , "value");
        //java.lang.Object cannot be cast to java.lang.String
        System.out.println(p.getProperty((String) keyObj));
    }
    public static void main(String[] args) {
        inconsistencyCausedByDefaultValue();
        destroyBySuperClass();
    }
}
