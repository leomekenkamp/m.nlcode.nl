package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
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
public class MidiInOutUiListView extends ListView<MidiInOutUi<?>> implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ObjectProperty<MidiInOutUi<?>> ownerProperty = new SimpleObjectProperty();

    private ObjectProperty<ObservableList<MidiInOutUi<?>>> availableMidiInOutUiListProperty = new SimpleObjectProperty<>();

    private ObservableList<MidiInOutUi<?>> midiInOutUiList = FXCollections.observableArrayList(MidiInOutUi.NAME_EXTRACTOR);

    private ListChangeListener<MidiInOutUi<?>> availableMidiInOutUiListChangeListener = new ListChangeListener<>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends MidiInOutUi<?>> change) {
            while (change.next()) {
                if (change.wasPermutated()) {
                } else if (change.wasUpdated()) {
                } else if (change.wasReplaced()) {
                } else {
                    if (change.wasRemoved()) {
                        for (MidiInOutUi removed : change.getRemoved()) {
                            LOGGER.debug("not longer available, removed <{}> from <{}>", removed.getMidiInOut(), getOwner());
//                        int remoteOutIndex = removed.getOutputListView().getItems().indexOf(removed);
//                        if (remoteOutIndex != -1) {
//                            removed.getOutputListView().getSelectionModel().clearSelection(remoteOutIndex);
//                        }
//                        int remoteInIndex = removed.getInputListView().getItems().indexOf(removed);
//                        if (remoteInIndex != -1) {
//                            removed.getInputListView().getSelectionModel().clearSelection(remoteInIndex);
//                        }
                            midiInOutUiList.remove(removed);
                        }
                    }
                    if (change.wasAdded()) {
                        for (MidiInOutUi<?> added : change.getAddedSubList()) {
                            if (added == getOwner()) {
                                LOGGER.debug("skipping owner");
                            } else {
                                LOGGER.debug("newly available, added <{}> to <{}>", added.getMidiInOut(), getOwner());
                                midiInOutUiList.add(added);
                            }
                        }
                    }
                }
            }
        }
    };

    public MidiInOutUiListView() {
        setItems(midiInOutUiList.sorted(MidiInOutUi.BY_NAME));
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setCellFactory(ly -> new ListCell<MidiInOutUi<?>>() {
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
                LOGGER.debug("items changed to <{}>", newValue);
                midiInOutUiListToItems();
            }
        });

        availableMidiInOutUiListProperty.addListener((ov, oldValue, newValue) -> {
            LOGGER.debug("midiInOutListProperty changed from <{}> to <{}>", oldValue, newValue);
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
                midiInOutUiList.add(oldValue);
            }
            midiInOutUiList.remove(newValue);
        });
//        prefHeightProperty().bind(widthProperty().divide(2));
    }

    private void midiInOutUiListToItems() {
        midiInOutUiList.clear();
        for (MidiInOutUi midiInOutUi : getAvailableMidiInOutUiList()) {
            if (midiInOutUi != getOwner()) {
                midiInOutUiList.add(midiInOutUi);
            }
        }
    }

    public ObjectProperty<MidiInOutUi<?>> ownerProperty() {
        return ownerProperty;
    }

    public void setOwner(MidiInOutUi<?> owner) {
        ownerProperty.set(owner);
    }

    /*
    FIXME: get rid of this: the list with the items should know who the owner is and not add it
     */
    public MidiInOutUi<?> getOwner() {
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
