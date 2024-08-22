package nl.nlcode.m.linkui;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author leo
 */
public class IntUpdater<U, H extends Updater.Holder<U>> extends Updater<Integer, U, H> {

    private AtomicInteger ref = new AtomicInteger();

    private int min;

    private int max;

    public IntUpdater(String name, H holder) {
        super(name, holder);
    }
    
    public IntUpdater(String name, H holder, int value) {
        this(holder, value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntUpdater(String name, H holder, int value, int min, int max) {
        super(name);
        this.min = min;
        this.max = max;
        set(value);
        register(holder);
    }

    // FIXME: remove 3 ctors below
    @Deprecated
    public IntUpdater(H holder) {
        this(TODO_NAME, holder);
    }
    
    @Deprecated
    public IntUpdater(H holder, int value) {
        this(TODO_NAME, holder, value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Deprecated
    public IntUpdater(H holder, int value, int min, int max) {
        this(TODO_NAME, holder, value, min, max);
    }

    public int get() {
        return ref.get();
    }

    public void set(int newValue) {
        set(newValue, null);
    }

    public void set(int newValue, Listener sendMeNoUpdate) {
        if (newValue < min || newValue > max) {
            throw new IllegalArgumentException("value must be in interval [" + min + "," + max + "]");
        }
        int oldValue = ref.getAndSet(newValue);
        runAfterChange(oldValue, newValue, sendMeNoUpdate);
    }

    public void withReference(Consumer<AtomicInteger> consumer) {
        int oldValue = ref.get();
        consumer.accept(ref);
        runAfterChange(oldValue, ref.get());
    }

    public int withReference(Function<AtomicInteger, Integer> func) {
        int oldValue = ref.get();
        int result = func.apply(ref);
        runAfterChange(oldValue, result);
        return result;
    }

    public static final int[] toIntArray(IntUpdater<?, ?>[] source) {
        int[] result = new int[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = source[i].get();
        }
        return result;
    }

    @Override
    public Integer getValue() {
        return get();
    }

    @Override
    public void setValue(Integer newValue) {
        set(newValue);
    }
}
