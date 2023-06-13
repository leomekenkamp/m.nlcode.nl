package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javax.sound.midi.MidiDevice;
import nl.nlcode.m.engine.MidiDeviceLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class MidiDeviceLinkUi extends MidiInOutUi<MidiDeviceLink> implements MidiDeviceLink.Ui{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private MidiDeviceSelector midiDeviceSelector;

    public MidiDeviceLinkUi(ProjectUi projectUi, MidiDeviceLink midiDeviceLink, MenuItem menuItem) {
        super(projectUi, midiDeviceLink, menuItem);
        loadFxml(MidiDeviceLinkUi.class, App.MESSAGES);
    }

    protected void handleInitialize() {
        super.handleInitialize();
        midiDeviceSelector.setItems(getProjectUi().getControlUi().getMidiDeviceMgr().getOpenMidiDevices());
        midiDeviceChanged();
        syncActiveSenderReceiver();
        midiDeviceSelector.getComboBox().valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setMidiDevice(newValue);
            syncActiveSenderReceiver();
            getProjectUi().setDirty();
        });
    }

    @Override
    public void midiDeviceChanged() {
        MidiDevice selected = getMidiInOut().getMidiDevice();
        if (selected == null) {
            selected = MidiDeviceSelector.NONE;
        }
        midiDeviceSelector.getComboBox().setValue(selected);
    }

}
