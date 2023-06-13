package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javax.sound.midi.ShortMessage;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.MidiSequencer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiSequencerUi extends MidiInOutUi<MidiSequencer> implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Color TRANSPARENT = Color.rgb(0, 0, 0, 0);

    public MidiSequencerUi(ProjectUi projectUi, MidiSequencer midiSequencer, MenuItem menuItem) {
        super(projectUi, midiSequencer, menuItem);
        loadFxml(MidiSequencerUi.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
    }

}
