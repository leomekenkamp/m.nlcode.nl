package nl.nlcode.m.ui;

import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import nl.nlcode.m.engine.KeyboardKeyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class KeyboardKeyboardUi extends MidiInOutUi<KeyboardKeyboard> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyboardKeyboardUi.class);

    @FXML
    private KeyboardKeyboardController kkController;

    public KeyboardKeyboardUi(ProjectUi projectUi, KeyboardKeyboard keyboardKeyboard, MenuItem menuItem) {
        super(projectUi, keyboardKeyboard, menuItem);
        loadFxml(App.MESSAGES);
    }

    @Override
    protected void doInit() {
        super.doInit();
        kkController.setParent(this);
    }


}
