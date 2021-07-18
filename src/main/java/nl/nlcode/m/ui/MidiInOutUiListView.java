package nl.nlcode.m.ui;

import java.util.Collections;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import nl.nlcode.javafxutil.FxmlController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiInOutUiListView extends ListView<MidiInOutUi> implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiInOutUiListView.class);

    private ObjectProperty<MidiInOutUi> ownerProperty = new SimpleObjectProperty();

    private ObjectProperty<ObservableList<MidiInOutUi<?>>> availableMidiInOutUiListProperty = new SimpleObjectProperty<>();

    private ListChangeListener<MidiInOutUi<?>> availableMidiInOutUiListChangeListener = new ListChangeListener<>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends MidiInOutUi<?>> change) {
            LOGGER.debug("change {} on {} ", change, change.getList());
            boolean sort = false;
            while (change.next()) {
                for (MidiInOutUi removed: change.getRemoved()) {
                    LOGGER.debug("removed: {}", removed);
                    getItems().remove(removed);
                }
                for (MidiInOutUi<?> added : change.getAddedSubList()) {
                    if (added == getOwner()) {
                        LOGGER.debug("skipping owner");
                    } else {
                        LOGGER.debug("added: {}", added);
                        // DEBUG
                        if (added == null) {
                            throw new IllegalArgumentException();
                        }
                        getItems().add(added);
                        sort = true;
                    }
                }
                sort = sort || change.wasPermutated() || change.wasUpdated();
            }
            sortItems();
        }
    };

    public MidiInOutUiListView() {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setCellFactory(ly -> new ListCell<MidiInOutUi>() {
            @Override
            public void updateItem(MidiInOutUi midiInOutUi, boolean empty) {
                super.updateItem(midiInOutUi, empty);
                if (empty || midiInOutUi == null) {
                    setText(null);
                } else {
                    setText(midiInOutUi.getName());
                }
            }
        });

        itemsProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue != null) {
                LOGGER.debug("items changed to {}", newValue);
                midiInOutUiListToItems();
            }
        });

        availableMidiInOutUiListProperty.addListener((ov, oldValue, newValue) -> {
            LOGGER.debug("midiInOutListProperty changed from {} to {} ", oldValue, newValue);
            if (oldValue != null) {
                oldValue.removeListener(availableMidiInOutUiListChangeListener);
            }
            if (newValue != null) {
                midiInOutUiListToItems();
                newValue.addListener(availableMidiInOutUiListChangeListener);
            }
        });

        ownerProperty.addListener((ov, oldValue, newValue) -> {
            if (oldValue != null) {
                getItems().add(oldValue);
            }
            getItems().remove(newValue);
            sortItems();
        });
//        prefHeightProperty().bind(widthProperty().divide(2));
    }

    private void midiInOutUiListToItems() {
//        if (true) throw new IllegalStateException("HERE!");
        getItems().clear();
        for (MidiInOutUi midiInOutUi : getAvailableMidiInOutUiList()) {
            if (midiInOutUi != getOwner()) {
                getItems().add(midiInOutUi);
            }
        }
        sortItems();
    }

    private void sortItems() {
        Collections.sort(getItems(), (left, right) -> {
            return left.getMidiInOut().getName().compareTo(right.getMidiInOut().getName());
        });
    }

    public ObjectProperty<MidiInOutUi> ownerProperty() {
        return ownerProperty;
    }

    public void setOwner(MidiInOutUi owner) {
        ownerProperty.set(owner);
    }

    public MidiInOutUi getOwner() {
        return ownerProperty.get();
    }

    public ObjectProperty<ObservableList<MidiInOutUi<?>>> availableMidiInOutUiListProperty() {
        return availableMidiInOutUiListProperty;
    }

    public ObservableList<MidiInOutUi<?>> getAvailableMidiInOutUiList() {
        return availableMidiInOutUiListProperty.get();
    }

    public void setAvailableMidiInOutUiList(ObservableList<MidiInOutUi<?>> availableMidiInOutUiList) {
        availableMidiInOutUiListProperty.set(availableMidiInOutUiList);
    }

}
