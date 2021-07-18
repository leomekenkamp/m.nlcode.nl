package nl.nlcode.m.ui;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

/**
 *
 * @author leo
 */
public class ListContent<T> {

    private ObservableList<T> list;

    private StringConverter<T> converter;

    public ListContent(ObservableList<T> list, StringConverter<T> converter) {
        this.list = list;
        this.converter = converter;
    }

    public ListContent(ObservableList<T> list, Function<T, String> itemToString, BiPredicate<T, String> matchFromList) {
        this(list, new StringConverter<T>() {

            @Override
            public String toString(T t) {
                return itemToString.apply(t);
            }

            @Override
            public T fromString(String string) {
                return list.stream().filter(new Predicate<T>() {
                    @Override
                    public boolean test(T t) {
                        return matchFromList.test(t, string);
                    }
                }).findAny().get();
            }

        });
    }

    public StringConverter<T> getConverter() {
        return converter;
    }

    public ObservableList<T> getList() {
        return list;
    }
}
