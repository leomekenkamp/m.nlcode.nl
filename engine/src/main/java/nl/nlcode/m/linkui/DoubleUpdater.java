package nl.nlcode.m.linkui;

import java.util.Objects;

/**
 *
 * @author leo
 */
public class DoubleUpdater<U, H extends Updater.Holder<U>> extends Updater<Double, U, H> {

    private volatile double value;

    private transient double min;

    private transient double max;

    public DoubleUpdater(H holder, double value) {
        this(holder, value, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public DoubleUpdater(H holder, double value, double min, double max) {
        this.min = min;
        this.max = max;
        set(value);
        register(holder);
    }

    public double get() {
        return value;
    }

    public void set(double newValue) {
        set(newValue, null);
    }

    public void set(double newValue, Listener sendMeNoUpdate) {
        if (newValue < min || newValue > max) {
            throw new IllegalArgumentException("value must be between <" + min + "> and <" + max + ">");
        }
        double oldValue = value;
        value = newValue;
        runAfterChange(oldValue, newValue, sendMeNoUpdate);
    }

    public static final double[] toDoubleArray(DoubleUpdater<?, ?>[] source) {
        double[] result = new double[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = source[i].get();
        }
        return result;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public void setValue(Double newValue) {
        this.value = newValue;
    }
}
