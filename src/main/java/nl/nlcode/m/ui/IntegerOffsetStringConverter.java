package nl.nlcode.m.ui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.converter.IntegerStringConverter;

/**
 * For cases where internally we use 0-based data, but in the user interface we use 1-based.
 * Fine example is MIDI channel: internally [0..15], but represented as [1..16
 * <p>
 * TODO: address int wraparound issues
 * 
 * @author leo
 */
public class IntegerOffsetStringConverter extends IntegerStringConverter {

    private IntegerProperty offset = new SimpleIntegerProperty(0);
    
    public int getOffset() {
        return offset.get();
    }
    
    public void setOffset(int offset) {
        this.offset.set(offset);
    }
    
    public IntegerProperty offsetProperty() {
        return offset;
    }
    
    @Override
    public Integer fromString(String string) {
        return - getOffset() + super.fromString(string);
    }

    @Override
    public String toString(Integer intgr) {
        return super.toString(getOffset() + intgr);
    }
    
}
