package nl.nlcode.m.engine;

import java.io.Serializable;

/**
 *
 * @author leo
 */
public class IntInterval {

    private int low = 0;

    private int high = 127;

    private int min = 0;

    private int max = 127;

    private boolean reversible = true;

    private IntervalClosure intervalClosure = IntervalClosure.CLOSED;

    public boolean contains(int value) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("value must be >= min and <= max");
        }
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        if (low < min) {
            throw new IllegalArgumentException("low must be >= min");
        }
        if (low > max ) {
            throw new IllegalArgumentException("low must be <= max");
        }
        if (high < min) {
            throw new IllegalArgumentException("high must be >= min");
        }
        if (high > max ) {
            throw new IllegalArgumentException("high must be <= max");
        }
        
        boolean lowOk = intervalClosure.isLowClosed() ? low <= value : low < value;
        boolean highOk = intervalClosure.isHighClosed() ? value <= high : value < high;
        if (low <= high) {
            return lowOk && highOk;
        } else if (!reversible) {
            throw new IllegalArgumentException("low must be <= high when nont-reversible");
        } else {
            return lowOk || highOk;
        }
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isReversible() {
        return reversible;
    }

    public void setReversible(boolean reversible) {
        this.reversible = reversible;
    }

    public IntervalClosure getIntervalClosure() {
        return intervalClosure;
    }

    public void setIntervalClosure(IntervalClosure intervalClosure) {
        if (intervalClosure == null) {
            throw new IllegalArgumentException();
        }
        this.intervalClosure = intervalClosure;
    }

}
