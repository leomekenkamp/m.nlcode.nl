package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import nl.nlcode.m.engine.MessageTypeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MessageTypeFilterUi extends MidiInOutUi<MessageTypeFilter> implements MessageTypeFilter.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private CheckBox filterNoteOn;
    private BooleanPropertyUpdaterBridge filterNoteOnBackend;

    @FXML
    private CheckBox filterNoteOff;
    private BooleanPropertyUpdaterBridge filterNoteOffBackend;

    @FXML
    private CheckBox filterTimerClock;
    private BooleanPropertyUpdaterBridge filterTimerClockBackend;

    @FXML
    private CheckBox filterSysEx;
    private BooleanPropertyUpdaterBridge filterSysExBackend;

    @FXML
    private CheckBox filterControllers;
    private BooleanPropertyUpdaterBridge filterControllersBackend;

    @FXML
    private CheckBox filterProgramChange;
    private BooleanPropertyUpdaterBridge filterProgramChangeBackend;

    public MessageTypeFilterUi(ProjectUi projectUi, MessageTypeFilter messageTypeFilter, MenuItem menuItem) {
        super(projectUi, messageTypeFilter, menuItem);
        loadFxml(MessageTypeFilterUi.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        filterNoteOnBackend = BooleanPropertyUpdaterBridge.create(getMidiInOut().filterNoteOn(), filterNoteOn.selectedProperty());
        filterNoteOffBackend = BooleanPropertyUpdaterBridge.create(getMidiInOut().filterNoteOff(), filterNoteOff.selectedProperty());
        filterTimerClockBackend = BooleanPropertyUpdaterBridge.create(getMidiInOut().filterTimerClock(), filterTimerClock.selectedProperty());
        filterControllersBackend = BooleanPropertyUpdaterBridge.create(getMidiInOut().filterControllers(), filterNoteOff.selectedProperty());
        filterProgramChangeBackend = BooleanPropertyUpdaterBridge.create(getMidiInOut().filterProgramChange(), filterNoteOff.selectedProperty());
    }

}
