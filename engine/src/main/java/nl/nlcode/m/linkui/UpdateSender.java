package nl.nlcode.m.linkui;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 *
 * @author leo
 * @param <T> type of the reference that will be reported on, type of the referenced value
 */
public abstract class UpdateSender<T> {

    public interface Listener<T> {
        /**
         * 
         * @param oldValue there is no guarantee whatsoever that this value is valid or non-null
         * @param newValue
         * @param updateSender 
         */
        void onValueChange(T oldValue, T newValue, UpdateSender<T> updateSender);
    }
    
    private T value;
    
    private Set<Listener<T>> listeners;
    
    protected UpdateSender() {
        this.listeners = Collections.newSetFromMap(new WeakHashMap<>());
    }

    public final T getValue() {
        return value;
    };

    public final void setValue(T newValue) {
        if (!equalsCurrent(newValue)) {
            if (validate(newValue)) {
                T oldValue = value;
                value = newValue;
                valueChangedFrom(oldValue);
            }
        }
    }
    
    /**
     * Validate the new value. 
     * 
     * @param newValue
     * @return true iff new value is allowed
     */
    protected boolean validate(T newValue) {
        return true;
    }

    /**
     * Tests if the new value is indeed new.
     * 
     * @param newValue
     * @return true iff new value is allowed
     */
    protected boolean equalsCurrent(T newValue) {
        return Objects.equals(getValue(), newValue);
    }
            
    private final void valueChangedFrom(T oldValue) {
        listeners.stream().forEach(listener -> notifyChange(oldValue, listener));
    }
    
    private final void notifyChange(T oldValue, Listener listener) {
        listener.onValueChange(oldValue, value, this);
    }
    
    public final void addListener(Listener<T> listener) {
        listeners.add(listener);
        listener.onValueChange(null, value, this);
    }

    public final void removeListener(Listener<T> listener) {
        listeners.remove(listener);
    }

}
