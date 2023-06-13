package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import nl.nlcode.m.engine.A42;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class A42Ui extends MidiInOutUi<A42> implements A42.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public A42Ui(ProjectUi projectUi, A42 a42, MenuItem menuItem) {
        super(projectUi, a42, menuItem);
        loadFxml(A42Ui.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        
    }

    @FXML
    public void allNotesOffManually() {
        getMidiInOut().panicManually();
    }
    
    @FXML
    public void allNotesOff() {
        getMidiInOut().allNotesOff();
    }
    
    @FXML
    public void allSoundOff() {
        getMidiInOut().allSoundOff();
    }
    
}
