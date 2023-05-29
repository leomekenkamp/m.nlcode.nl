package nl.nlcode.m.ui;

import javafx.beans.NamedArg;
import javafx.util.converter.IntegerStringConverter;

/**
 *
 * @author leo
 */
public class IntegerRangeStringConverter extends IntegerStringConverter {

    private final int min;
    private final int max;
    private final String noneString;
    private final int noneValue;

    /**
     *
     * @param min
     * @param max
     * @param none leave {@code null} for ranged value only
     * @param noneValue
     */
    public IntegerRangeStringConverter(@NamedArg("min") int min, @NamedArg("max") int max, @NamedArg("noneString") String noneString, @NamedArg("noneValue") int noneValue) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        if (noneString != null && noneValue >= min && noneValue <= max) {
            throw new IllegalArgumentException("none must not be in range [min,max]");
        }
        this.min = min;
        this.max = max;
        this.noneString = noneString;
        this.noneValue = noneValue;
    }

    /**
     *
     * @param min
     * @param max
     * @param none leave {@code null} for ranged value only
     * @param noneValue
     */
    public IntegerRangeStringConverter(@NamedArg("min") int min, @NamedArg("max") int max, @NamedArg("noneString") String noneString) {
        this(min, max, noneString, Integer.MIN_VALUE);
    }

    @Override
    public Integer fromString(String from) {
        if (noneString != null && noneString.equals(from)) {
            return noneValue;
        } else {
            try {
                Integer fromSuper = super.fromString(from);
                if (fromSuper < min) {
                    return min;
                } else if (fromSuper > max) {
                    return max;
                } else {
                    return fromSuper;
                }
            } catch (NumberFormatException e) {
                throw new UserMadeAMistakeException(e);
            }
        }
    }

    @Override
    public String toString(Integer from) {
        if (noneString != null && noneValue == from) {
            return noneString;
        } else {
            return super.toString(from);
        }
    }

}
