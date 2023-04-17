package nl.nlcode.m.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import nl.nlcode.m.engine.MidiChannelMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiChannelMatrixUi extends MidiInOutUi<MidiChannelMatrix> {
    
    @FXML
    private MidiChannelMatrixController defaultController;

    public MidiChannelMatrixUi(ProjectUi projectUi, MidiChannelMatrix midiChannelMatrix, MenuItem menuItem) {
        super(projectUi, midiChannelMatrix, menuItem);
        loadFxml(App.MESSAGES);
    }   

     @Override
    protected void doInit() {
        super.doInit();
        defaultController.setParent(this);
    }

}
