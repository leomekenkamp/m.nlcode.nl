package nl.nlcode.m.linkui;

import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 *
 * @author leo
 * @param <U> user interface component to manipulate when this property has changed.
 * @param <H> acts as link between {@code UpdateProperty} and the UI component(s)
 */
public abstract class UpdateProperty<T, U, H extends UpdateProperty.Holder<U>> {

    /**
     * The holder for the UI component. The holder can hold any non-negative number of UI
     * interfaces.
     *
     * @param <U> user interface component
     */
    public static interface Holder<U> {

        /**
         * Runs the given action on all (which may be none) of the connected UI components.
         *
         * @param action
         */
        void uiUpdate(Consumer<U> action);

        void register(UpdateProperty<?, U, ? extends Holder<U>> updateProperty);

        void unregister(UpdateProperty<?, U, ? extends Holder<U>> updateProperty);

        void setDirty();

        PropertyChangeSupport getPropertyChangeSupport();
    }

    /**
     * Listeners will be weakly referenced.
     *
     * @param <T>
     */
    public static interface Listener<T> {

        void updatePropertyValueChanged(T oldValue, T newValue);
    }

    private H holder;

    private boolean holderDirtyOnChange = true;

    private Consumer<U> afterSet;

    private final Map<Listener, ?> listeners = new HashMap<>(); // new WeakHashMap<>();

    protected UpdateProperty() {
    }

    public abstract T getValue();

    public abstract void setValue(T newValue);

    public final void setAfterChange(Consumer<U> afterSet) {
        this.afterSet = afterSet;
    }

    public final void setAfterChange(H holder, Consumer<U> afterSet) {
        register(holder);
        setAfterChange(afterSet);
    }

    /**
     * Listeners are weakly referenced.
     *
     * @param listener
     */
    public void addListener(Listener listener) {
        listeners.put(listener, null);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public final void register(H holder) {
        if (this.holder != null) {
            unregister();
        }
        this.holder = holder;
        if (holder != null) {
            holder.register(this);
            runAfterChange();
        }
    }

    public final void unregister() {
        holder.unregister(this);
        holder = null;
    }

    /**
     * Executes the action specified by {@code setAfterSet(H, Comsumer<U>}.Will be called 1) when
     * the value in this property has (potentially) changed, 2) when the holder {@code H} wants to
     * update the state of its connected UI with this state, e.g. when an UI is connected for the
     * first time.
     *
     * @param sendMeNoUpdate Since we are bridging threads here, if one side updates 'too fast',
     * then the other side may not have caught on, leading to two threads changing each other in a
     * feedback loop. This parameter provides a way to prevent such a feedback. Example: Thread A
     * and B, syncing value V. If we are unlucky, we will see (A:V0,B:V0 > A:V1,B:V0 > A:V2,B:V1 >
     * A:V1,B:V2 > A:V2,B:V1 > A:V1,B:V2 (etc).
     */
    public final void runAfterChange(T oldValue, T newValue, Listener sendMeNoUpdate) {
        if (!Objects.equals(oldValue, newValue)) {
            if (holder != null) {
                if (holderDirtyOnChange) {
                    holder.setDirty();
                }
                if (afterSet != null) {
                    holder.uiUpdate(afterSet);
                }
            }
            listeners.keySet().stream().forEach(cl -> {
                if (cl != sendMeNoUpdate) {
                    cl.updatePropertyValueChanged(oldValue, newValue);
                }
            });
        }
    }

    public final void runAfterChange(T oldValue, T newValue) {
        runAfterChange(oldValue, newValue, null);
    }

    public final void runAfterChange() {
        runAfterChange(null, getValue(), null);
    }

    public boolean isHolderDirtyOnChange() {
        return holderDirtyOnChange;
    }

    public void setHolderDirtyOnChange(boolean holderDirtyOnChange) {
        this.holderDirtyOnChange = holderDirtyOnChange;
    }

}
