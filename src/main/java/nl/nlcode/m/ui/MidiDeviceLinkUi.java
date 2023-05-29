package nl.nlcode.m.ui;

import java.util.logging.Level;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.ShortMessage;
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

    protected void handleInitialize() {
        super.handleInitialize();
        midiDeviceSelector.setItems(getProjectUi().getControlUi().getMidiDeviceMgr().getOpenMidiDevices());
        MidiDevice selected = getMidiInOut().getMidiDevice();
        if (selected == null) {
            selected = MidiDeviceSelector.NONE;
            // todo: add some nice ui field to indicate the selected name
            LOGGER.debug("previous not linked (or device not open / not found), so linking to 'none'");
        } else {
            LOGGER.debug("previously linked to <{}>", selected);
        }
        midiDeviceSelector.getComboBox().setValue(selected);
        syncActiveSenderReceiver();
        midiDeviceSelector.getComboBox().valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setMidiDevice(newValue);
            if (newValue != null) {
                try {
                    ShortMessage m = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 0, 48, 0);
                    getMidiInOut().asyncReceive(m, 0);
                } catch (InvalidMidiDataException ex) {
                    java.util.logging.Logger.getLogger(MidiDeviceLinkUi.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            syncActiveSenderReceiver();
            getProjectUi().setDirty();
        });
    }

}
