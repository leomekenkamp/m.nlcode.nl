package nl.nlcode.m.linkui;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author leo
 */
public class ObjectUpdateProperty<T, U, H extends UpdateProperty.Holder<U>> extends UpdateProperty<T, U, H> {

    private AtomicReference<T> ref = new AtomicReference<>();

    public ObjectUpdateProperty(T value) {
        ref.set(value);
    }

    public T get() {
        return ref.get();
    }

    public void set(T newValue) {
        set(newValue, null);
    }
    
    public void set(T newValue, UpdateProperty.Listener<T> sendMeNoUpdate) {
        T oldValue = ref.getAndSet(newValue);
        runAfterChange(oldValue, newValue, sendMeNoUpdate);
    }
    
    public static final <T> T[] toObjectArray(ObjectUpdateProperty<T, ?, ?>[] source, Class<T> type) {
        T[] result = (T[]) Array.newInstance(type, source.length);
        for (int i = 0; i < source.length; i++) {
            result[i] = source[i].get();
        }
        return result;
    }

    @Override
    public T getValue() {
        return ref.get();
    }

    @Override
    public void setValue(T newValue) {
        set(newValue, null);
    }

}
