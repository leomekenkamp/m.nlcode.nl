package nl.nlcode.m.ui;

import javafx.collections.ListChangeListener;

/**
 *
 * @author leo
 */
public class ListChangeListenerHelper<E> {

    public void process(ListChangeListener.Change<? extends E> change) {
        while (change.next()) {
            if (change.wasPermutated()) {
//                for (int newIndex = change.getFrom(); newIndex <= change.getTo(); newIndex++) {
//                    permutated(newIndex, change.getList().get(i));
//                }
                // too bloody complex
            } else if (change.wasUpdated()) {
                for (int i = change.getFrom(); i <= change.getTo(); i++) {
                    updated(change.getList().get(i));
                }
//            } else if (change.wasReplaced()) {
            } else {
                if (change.wasRemoved()) {
                    for (E removed : change.getRemoved()) {
                        if (removed != null) { // happens for some SelectionModel changes
                            removed(removed);
                        }
                    }
                }
                if (change.wasAdded()) {
                    for (E added : change.getAddedSubList()) {
                        if (added != null) { // happens for some SelectionModel changes
                            added(added);
                        }
                    }
                }
            }
        }
    }

    public void removed(E removed) {
    }

    public void added(E added) {
    }

    public void updated(E updated) {
    }

}
