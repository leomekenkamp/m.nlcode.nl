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
                    if (change.wasRemoved()) {
                        // change.getRemoved() is buggy, use indices instead
                       for (int localIndex = change.getFrom(); localIndex <= change.getTo(); localIndex ++) {
                           T removed = list.get(localIndex);
                            target.removeAll(removed);
                       }
                    }
                    if (change.wasAdded()) {
                        target.addAll(change.getAddedSubList());
                    }
                }
            });
        }
    }
}
