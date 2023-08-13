package nl.nlcode.m.linkui;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 *
 * @author leo
 */
public class LongUpdateProperty<U, H extends UpdateProperty.Holder<U>> extends UpdateProperty<Long, U, H> {

    private AtomicLong ref = new AtomicLong();

    private LongConsumer validator;

    public LongUpdateProperty(H holder, long value) {
        ref.set(value);
        register(holder);
    }

    public long get() {
        return ref.get();
    }

    public void set(long newValue) {
        set(newValue, null);
    }
    
    public void set(long newValue, Listener sendMeNoUpdate) {
        if (validator != null) {
            validator.accept(newValue);
        }
        long oldValue = ref.getAndSet(newValue);
        runAfterChange(oldValue, newValue, sendMeNoUpdate);
    }

    public LongConsumer getValidator() {
        return validator;
    }

    public void setValidator(LongConsumer validator) {
        if (validator != null) {
            validator.accept(ref.get());
        }
        this.validator = validator;
    }

    public void withReference(Consumer<AtomicLong> consumer) {
        long prevValue = ref.get();
        consumer.accept(ref);
        runAfterChange(prevValue, ref.get());
    }
    
    public <T> T withReference(Function<AtomicLong, T> func) {
        long prevValue = ref.get();
        T result = func.apply(ref);
        runAfterChange(prevValue, ref.get());
        return result;
    }
    
    public static final long[] toLongArray(LongUpdateProperty<?, ?>[] source) {
        long[] result = new long[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = source[i].get();
        }
        return result;
    }

    @Override
    public Long getValue() {
        return ref.get();
    }

    @Override
    public void setValue(Long newValue) {
        ref.set(newValue);
    }
}
