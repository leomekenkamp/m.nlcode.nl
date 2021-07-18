package nl.nlcode.m.ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author leo
 */
public interface ObservableUtil {

    static <T> void merge(ObservableList<T> target, ObservableList<? extends T>... lists) { 
        for (ObservableList<? extends T> list : lists) {
            target.addAll(list);
            list.addListener((ListChangeListener.Change<? extends T> change) -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        target.addAll(change.getAddedSubList());
                    }
                    if (change.wasRemoved()) {
                        target.removeAll(change.getRemoved());
                    }
                }
            });
        }
    }
}
