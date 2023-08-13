package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import nl.nlcode.javafxutil.FxmlController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiInOutUiListView extends ListView<MidiInOutUi<?>> implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ObservableList<MidiInOutUi<?>> itemsBacking;

    private final ObjectProperty<MidiInOutUi<?>> ownerProperty = new SimpleObjectProperty();

    private final ObjectProperty<ObservableList<MidiInOutUi<?>>> availableMidiInOutUiListProperty = new SimpleObjectProperty<>();

    private final Map<MidiInOutUi<?>, BooleanProperty> midiInOutUiToBoolProp = new HashMap<>();

    // Contents may only be changed by SimpleBooleanProperty in values of midiInOutUiToBoolProp.
    private final ObservableList<MidiInOutUi<?>> checked = FXCollections.observableArrayList();

    private final ObservableList<MidiInOutUi<?>> checkedReadonly = FXCollections.unmodifiableObservableList(checked);
    
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
                            itemsBacking.remove(removed);
                        }
                    }
                    if (change.wasAdded()) {
                        for (MidiInOutUi<?> added : change.getAddedSubList()) {
                            if (added == getOwner()) {
                                LOGGER.debug("skipping owner");
                            } else {
                                LOGGER.debug("newly available, added <{}> to <{}>", added.getMidiInOut(), getOwner());
                                itemsBacking.add(added);
                            }
                        }
                    }
                }
            }
        }
    };

    public MidiInOutUiListView() {
        // stop selection; not foolproof; might sometimes see brief selection of a row
        getSelectionModel().selectedIndexProperty().addListener((ov, oldvalue, newValue)
                -> Platform.runLater(()
                        -> getSelectionModel().clearSelection()
                )
        );

        itemsBacking = getItems();

        itemsBacking.addListener(
                new ListChangeListenerHelper<MidiInOutUi<?>>() {
            @Override
            public void removed(MidiInOutUi<?> removed) {
                checked.remove(removed);
                midiInOutUiToBoolProp.remove(removed, removed);
            }

            @Override
            public void added(MidiInOutUi<?> added) {
                SimpleBooleanProperty checkedProp = new SimpleBooleanProperty();
                checkedProp.addListener((ov, oldValue, newValue) -> {
                    if (newValue) {
                        checked.add(added);
                    } else {
                        checked.remove(added);
                    }
                });
                midiInOutUiToBoolProp.put(added, checkedProp);
            }

        });

        setItems(itemsBacking.sorted(MidiInOutUi.BY_NAME));

        itemsProperty()
                .addListener((ov, oldValue, newValue) -> {
                    throw new IllegalStateException("MUST NOT CHANGE ITEMS!!!");
//            if (newValue != null) {
//                LOGGER.debug("items changed to <{}>", newValue);
//                midiInOutUiListToItems();
//            }
                }
                );

        availableMidiInOutUiListProperty.addListener(
                (ov, oldValue, newValue) -> {
                    LOGGER.debug("midiInOutListProperty changed from <{}> to <{}>", oldValue, newValue);
                    if (oldValue != null) {
                        oldValue.removeListener(availableMidiInOutUiListChangeListener);
                    }
                    if (newValue != null) {
                        MidiInOutUiStringConverter conv = new MidiInOutUiStringConverter(newValue);
                        setCellFactory(list -> new CheckBoxListCell(midiInOutUi -> {
                    BooleanProperty check = midiInOutUiToBoolProp.get(midiInOutUi);
                    return check;
                }, conv));

                        midiInOutUiListToItems();
                        newValue.addListener(availableMidiInOutUiListChangeListener);
                    }
                }
        );

        ownerProperty.addListener(
                (ov, oldValue, newValue) -> {
                    if (oldValue != null) {
                        itemsBacking.add(oldValue);
                    }
                    itemsBacking.remove(newValue);
                }
        );

//        prefHeightProperty().bind(widthProperty().divide(2));
    }

    private void midiInOutUiListToItems() {
        itemsBacking.clear();
        for (MidiInOutUi midiInOutUi : getAvailableMidiInOutUiList()) {
            if (midiInOutUi != getOwner()) {
                itemsBacking.add(midiInOutUi);
            }
        }
    }

    public ObjectProperty<MidiInOutUi<?>> ownerProperty() {
        return ownerProperty;
    }

    public void setOwner(MidiInOutUi<?> owner) {
        ownerProperty.set(owner);
    }

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

    public ObservableList<MidiInOutUi<?>> getChecked() {
        return checkedReadonly;
    }

    public boolean isChecked(MidiInOutUi<?> midiInOutUi) {
        return midiInOutUiToBoolProp.get(midiInOutUi).get();
    }

    public void setChecked(MidiInOutUi<?> midiInOutUi, boolean checked) {
        midiInOutUiToBoolProp.get(midiInOutUi).set(checked);
    }

}
