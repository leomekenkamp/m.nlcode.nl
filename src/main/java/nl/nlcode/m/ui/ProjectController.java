package nl.nlcode.m.ui;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import nl.nlcode.m.engine.MidiInOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class ProjectController<T extends MidiInOut> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    private ProjectUi projectUi;
    private T midiInOut;
    private MenuItem menuItem;
    
    public ProjectController(ProjectUi projectUi, T midiInOut, MenuItem menuItem) {
        this.projectUi = projectUi;
        this.midiInOut = midiInOut;
        this.menuItem = menuItem;
    }
    
    @FXML
    private void initialize() {
    }

    public ProjectUi getProjectUi() {
        return projectUi;
    }
    
    public T getMidiInOut() {
        return midiInOut;
    }
    
    public MenuItem getMenuItem() {
        return menuItem;
    }
}
