package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javax.sound.midi.MidiDevice;
import nl.nlcode.m.engine.MidiDeviceLink;
import static nl.nlcode.m.engine.MidiDeviceMgr.COMPARE_BY_DISPLAY_NAME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static nl.nlcode.m.engine.MidiDeviceMgr.NONE_MIDI_DEVICE;

/**
 */
public class MidiDeviceLinkUi extends MidiInOutUi<MidiDeviceLink> implements MidiDeviceLink.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private ComboBox midiDeviceComboBox;

    private ListChangeListener<MidiDevice> listChangeListener;

    private static class MidiDeviceListChangeListener implements ListChangeListener<MidiDevice> {

        private ComboBox<MidiDevice> midiDeviceComboBox;

        MidiDeviceListChangeListener(ComboBox<MidiDevice> midiDeviceComboBox) {
            this.midiDeviceComboBox = midiDeviceComboBox;
        }

        @Override
        public void onChanged(ListChangeListener.Change<? extends MidiDevice> change) {
            while (change.next()) {
                if (change.wasPermutated()) {
                } else if (change.wasUpdated()) {
                } else if (change.wasReplaced()) {
                } else {
                    MidiDevice selected = midiDeviceComboBox.getValue();
                    for (MidiDevice removed : change.getRemoved()) {
                        if (selected == removed) {
                            selected = NONE_MIDI_DEVICE;
                        }
                        midiDeviceComboBox.getItems().remove(removed);
                    }
                    if (change.wasAdded()) {
                        midiDeviceComboBox.getItems().addAll(change.getAddedSubList());
                        midiDeviceComboBox.getItems().sort(COMPARE_BY_DISPLAY_NAME);
                    }
                    midiDeviceComboBox.setValue(selected);
                }
            }
        }
    }

    public MidiDeviceLinkUi(ProjectUi projectUi, MidiDeviceLink midiDeviceLink, MenuItem menuItem) {
        super(projectUi, midiDeviceLink, menuItem);
        loadFxml(MidiDeviceLinkUi.class, App.MESSAGES);
    }

    protected void handleInitialize() {
        super.handleInitialize();
        midiDeviceComboBox.getItems().add(NONE_MIDI_DEVICE);
        midiDeviceComboBox.setConverter(App.createMidiDeviceConverter(getProjectUi().getControlUi().getOpenMidiDevices()));
        listChangeListener = new MidiDeviceListChangeListener(midiDeviceComboBox);
        getProjectUi().getControlUi().getOpenMidiDevices().addListener(new WeakListChangeListener(listChangeListener));
        midiDeviceChanged();
        syncActiveSenderReceiver();
        midiDeviceComboBox.valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setMidiDevice((MidiDevice) newValue);
            syncActiveSenderReceiver();
            getProjectUi().setDirty();
        });
    }

    @Override
    public void midiDeviceChanged() {
        MidiDevice selected = getMidiInOut().getMidiDevice();
        if (selected == null) {
            selected = NONE_MIDI_DEVICE;
        }
        midiDeviceComboBox.setValue(selected);
    }

}
