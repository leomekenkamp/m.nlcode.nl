package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import nl.nlcode.m.engine.I18n;
import nl.nlcode.m.engine.NoteGateVelocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class NoteGateVelocityUi extends MidiInOutUi<NoteGateVelocity> implements NoteGateVelocity.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Spinner<Integer> fromVelocity;
    private IntPropertyUpdaterBridge fromVelocityBacking;

    @FXML
    private Spinner<Integer> toVelocity;
    private IntPropertyUpdaterBridge toVelocityBacking;

    public NoteGateVelocityUi(ProjectUi projectUi, NoteGateVelocity noteGate, MenuItem menuItem) {
        super(projectUi, noteGate, menuItem);
        loadFxml(NoteGateVelocityUi.class, I18n.msg());
    }

    protected void handleInitialize() {
        super.handleInitialize();
        fromVelocityBacking = IntPropertyUpdaterBridge.create(getMidiInOut().fromVelocity(), fromVelocity.getValueFactory().valueProperty());
        toVelocityBacking = IntPropertyUpdaterBridge.create(getMidiInOut().toVelocity(), toVelocity.getValueFactory().valueProperty());
    }
}
