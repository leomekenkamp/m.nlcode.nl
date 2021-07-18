package nl.nlcode.m.ui;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javax.sound.midi.MidiDevice;
import nl.nlcode.m.engine.MidiDeviceLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class MidiDeviceLinkUi extends MidiInOutUi<MidiDeviceLink> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiDeviceLinkUi.class);

    @FXML
    private MidiDeviceSelector midiDeviceSelector;

    public MidiDeviceLinkUi(ProjectUi projectUi, MidiDeviceLink midiDeviceLink, MenuItem menuItem) {
        super(projectUi, midiDeviceLink, menuItem);
        loadFxml(MidiDeviceLinkUi.class, App.MESSAGES);
    }

    protected void doInit() {
        super.doInit();
        midiDeviceSelector.setItems(getProjectUi().getControlUi().getMidiDeviceMgr().getOpenMidiDevices());
        MidiDevice selected = getMidiInOut().getMidiDevice();
        if (selected == null) {
            selected = MidiDeviceSelector.NONE;
            LOGGER.debug("previous not linked (or device not open / not found), so linking to 'none'");
        } else {
            LOGGER.debug("previously linked to {}", selected);
        }
        midiDeviceSelector.getComboBox().setValue(selected);
        syncActiveSenderReceiver();
        midiDeviceSelector.getComboBox().valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setMidiDevice(newValue);
            syncActiveSenderReceiver();
            getProjectUi().setDirty();
        });
    }

}
