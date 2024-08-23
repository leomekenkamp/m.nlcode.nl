package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.I18n;
import nl.nlcode.m.engine.Sequencer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class SequencerUi extends MidiInOutUi<Sequencer> implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Color TRANSPARENT = Color.rgb(0, 0, 0, 0);

    public SequencerUi(ProjectUi projectUi, Sequencer sequencer, MenuItem menuItem) {
        super(projectUi, sequencer, menuItem);
        loadFxml(SequencerUi.class, I18n.msg());
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
    }

}
