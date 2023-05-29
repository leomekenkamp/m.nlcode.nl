package nl.nlcode.m.ui;

import javafx.scene.control.MenuItem;
import nl.nlcode.m.engine.Arpeggiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class ArpeggiatorUi extends MidiInOutUi<Arpeggiator> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArpeggiatorUi.class);


    public ArpeggiatorUi(ProjectUi projectUi, Arpeggiator arpeggiator, MenuItem menuItem) {
        super(projectUi, arpeggiator, menuItem);
        loadFxml(ArpeggiatorUi.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
    }
}
