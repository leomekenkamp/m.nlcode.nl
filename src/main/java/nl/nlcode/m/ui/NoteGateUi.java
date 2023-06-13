package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
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
public class NoteGateUi extends MidiInOutUi<NoteGate> implements NoteGate.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Spinner<Integer> fromVelocity;
    private IntUpdatePropertyBridge fromVelocityBacking;

    @FXML
    private Spinner<Integer> toVelocity;
    private IntUpdatePropertyBridge toVelocityBacking;

    public NoteGateUi(ProjectUi projectUi, NoteGate noteGate, MenuItem menuItem) {
        super(projectUi, noteGate, menuItem);
        loadFxml(NoteGateUi.class, App.MESSAGES);
    }

    protected void handleInitialize() {
        super.handleInitialize();
        fromVelocityBacking = IntUpdatePropertyBridge.create(getMidiInOut().fromVelocity(), fromVelocity.getValueFactory().valueProperty());
        toVelocityBacking = IntUpdatePropertyBridge.create(getMidiInOut().toVelocity(), toVelocity.getValueFactory().valueProperty());
    }
}
