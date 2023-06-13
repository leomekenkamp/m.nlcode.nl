package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.beans.property.BooleanProperty;
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
    private BooleanUpdatePropertyBridge filterNoteOnBackend;

    @FXML
    private CheckBox filterNoteOff;
    private BooleanUpdatePropertyBridge filterNoteOffBackend;

    @FXML
    private CheckBox filterTimerClock;
    private BooleanUpdatePropertyBridge filterTimerClockBackend;

    @FXML
    private CheckBox filterSysEx;
    private BooleanUpdatePropertyBridge filterSysExBackend;

    @FXML
    private CheckBox filterControllers;
    private BooleanUpdatePropertyBridge filterControllersBackend;

    @FXML
    private CheckBox filterProgramChange;
    private BooleanUpdatePropertyBridge filterProgramChangeBackend;

    public MessageTypeFilterUi(ProjectUi projectUi, MessageTypeFilter messageTypeFilter, MenuItem menuItem) {
        super(projectUi, messageTypeFilter, menuItem);
        loadFxml(MessageTypeFilterUi.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        filterNoteOnBackend = BooleanUpdatePropertyBridge.create(getMidiInOut().filterNoteOn(), filterNoteOn.selectedProperty());
        filterNoteOffBackend = BooleanUpdatePropertyBridge.create(getMidiInOut().filterNoteOff(), filterNoteOff.selectedProperty());
        filterTimerClockBackend = BooleanUpdatePropertyBridge.create(getMidiInOut().filterTimerClock(), filterTimerClock.selectedProperty());
        filterControllersBackend = BooleanUpdatePropertyBridge.create(getMidiInOut().filterControllers(), filterNoteOff.selectedProperty());
        filterProgramChangeBackend = BooleanUpdatePropertyBridge.create(getMidiInOut().filterProgramChange(), filterNoteOff.selectedProperty());
    }

}
