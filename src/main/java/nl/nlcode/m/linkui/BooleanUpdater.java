package nl.nlcode.m.linkui;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author leo
 */
public class BooleanUpdater<U, H extends Updater.Holder<U>> extends Updater<Boolean, U, H> {

    private AtomicBoolean ref = new AtomicBoolean();

    public BooleanUpdater(H holder) {
        super(holder);
    }
    
    public BooleanUpdater(H holder, boolean v) {
        ref.set(v);
        register(holder);
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

    public static final boolean[] toBooleanArray(BooleanUpdater<?, ?>[] source) {
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
