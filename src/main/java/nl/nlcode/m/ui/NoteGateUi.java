package nl.nlcode.m.ui;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import nl.nlcode.m.engine.NoteGate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class NoteGateUi extends MidiInOutUi<NoteGate> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoteGateUi.class);

    @FXML
    private Spinner<Integer> fromVelocity;
    
    @FXML
    private Spinner<Integer> toVelocity;
    
    public NoteGateUi(ProjectUi projectUi, NoteGate noteGate, MenuItem menuItem) {
        super(projectUi, noteGate, menuItem);
        loadFxml(NoteGateUi.class, App.MESSAGES);
    }

    protected void handleInitialize() {
        super.handleInitialize();
        fromVelocity.getValueFactory().setValue(getMidiInOut().getFromVelocity());
        fromVelocity.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setFromVelocity(newValue);
        });
        toVelocity.getValueFactory().setValue(getMidiInOut().getToVelocity());
        toVelocity.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setToVelocity(newValue);
        });

    }
}