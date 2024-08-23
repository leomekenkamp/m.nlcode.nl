package nl.nlcode.m.ui;

import java.util.List;
import javafx.util.StringConverter;

/**
 *
 * @author leo
 */
public abstract class ListItemStringConverter<T> extends StringConverter<T> {

    private List<T> list;

    public ListItemStringConverter(List<T> list) {
        this.list = list;
    }

    @Override
    public abstract String toString(T t);

    @Override
    public T fromString(String string) {
        for (T t : list) {
            if (toString(t).equals(string)) {
                return t;
            }
        }
        return null;
    }

}
