package nl.nlcode.m.linkui;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author leo
 */
public class BooleanUpdateProperty<U, H extends UpdateProperty.Holder<U>> extends UpdateProperty<Boolean, U, H> {

    private AtomicBoolean ref = new AtomicBoolean();

    public BooleanUpdateProperty(boolean v) {
        ref.set(v);
    }

    public boolean get() {
        return ref.get();
    }

    public void set(boolean newValue) {
        set(newValue, null);
    }
    
    public void set(boolean newValue, Listener sendMeNoUpdate) {
        boolean oldValue = ref.getAndSet(newValue);
        runAfterChange(oldValue, newValue, sendMeNoUpdate);
    }

    public static final boolean[] toBooleanArray(BooleanUpdateProperty<?, ?>[] source) {
        boolean[] result = new boolean[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = source[i].get();
        }
        return result;
    }

    @Override
    public Boolean getValue() {
        return ref.get();
    }

    @Override
    public void setValue(Boolean newValue) {
        ref.set(newValue);
    }

}
