package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import nl.nlcode.m.engine.KeyboardKeyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class KeyboardKeyboardUi extends MidiInOutUi<KeyboardKeyboard> implements KeyboardKeyboard.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Map<KeyCode, Button> keyCodeToButton = new HashMap<>();

    @FXML
    private Button a0;

    @FXML
    private Button aSharp0;

    @FXML
    private Button b0;

    @FXML
    private Button c0;

    @FXML
    private Button cSharp0;

    @FXML
    private Button d0;

    @FXML
    private Button dSharp0;

    @FXML
    private Button e0;

    @FXML
    private Button f0;

    @FXML
    private Button fSharp0;

    @FXML
    private Button g0;

    @FXML
    private Button gSharp0;

    @FXML
    private Button a1;

    @FXML
    private Button aSharp1;

    @FXML
    private Button b1;

    @FXML
    private Button c1;

    @FXML
    private Button cSharp1;

    @FXML
    private Button d1;

    @FXML
    private Button dSharp1;

    @FXML
    private Button e1;

    @FXML
    private Spinner<Integer> velocity;
    private IntegerProperty velocityBackend;

    @FXML
    private Spinner<Integer> channel;
    private IntUpdatePropertyBridge channelBackend;

    @FXML
    private Spinner<Integer> octave;
    private IntegerProperty octaveBackend;

    @FXML
    private GridPane keyGrid;

    public KeyboardKeyboardUi(ProjectUi projectUi, KeyboardKeyboard keyboardKeyboard, MenuItem menuItem) {
        super(projectUi, keyboardKeyboard, menuItem);
        loadFxml(KeyboardKeyboardUi.class, App.MESSAGES);
    }

    protected void handleInitialize() {
        super.handleInitialize();
        KeyboardKeyboard midiInOut = getMidiInOut();
        bind(KeyCode.A, a0, 57, midiInOut);
        bind(KeyCode.W, aSharp0, 58, midiInOut);
        bind(KeyCode.S, b0, 59, midiInOut);
        bind(KeyCode.D, c0, 60, midiInOut);
        bind(KeyCode.R, cSharp0, 61, midiInOut);
        bind(KeyCode.F, d0, 62, midiInOut);
        bind(KeyCode.T, dSharp0, 63, midiInOut);
        bind(KeyCode.G, e0, 64, midiInOut);
        bind(KeyCode.H, f0, 65, midiInOut);
        bind(KeyCode.U, fSharp0, 66, midiInOut);
        bind(KeyCode.J, g0, 67, midiInOut);
        bind(KeyCode.I, gSharp0, 68, midiInOut);
        bind(KeyCode.K, a1, 69, midiInOut);
        bind(KeyCode.O, aSharp1, 70, midiInOut);
        bind(KeyCode.L, b1, 71, midiInOut);
        bind(KeyCode.SEMICOLON, c1, 72, midiInOut);
        bind(KeyCode.OPEN_BRACKET, cSharp1, 73, midiInOut);
        bind(KeyCode.QUOTE, d1, 74, midiInOut);
        bind(KeyCode.CLOSE_BRACKET, dSharp1, 75, midiInOut);
        bind(KeyCode.BACK_SLASH, e1, 76, midiInOut);

        c0.getParent().addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            Button button = keyCodeToButton.get(ev.getCode());
            if (button != null) {
                button.arm();
                ev.consume();
            }
        });
        c0.getParent().addEventHandler(KeyEvent.KEY_RELEASED, ev -> {
            Button button = keyCodeToButton.get(ev.getCode());
            if (button != null) {
                button.disarm();
                ev.consume();
            }
        });

        velocityBackend = IntUpdatePropertyBridge.create(getMidiInOut().velocity(), velocity.getValueFactory().valueProperty());
        octaveBackend = IntUpdatePropertyBridge.create(getMidiInOut().octave(), octave.getValueFactory().valueProperty());
        channelBackend = IntUpdatePropertyBridge.create(getMidiInOut().channel(), channel.getValueFactory().valueProperty());
        channel.getValueFactory().setConverter(getMidiChannelStringConverter());

        getMidiChannelStringConverter().offsetProperty().addListener((ov, oldValue, newValue) -> {
            channelBackend.refresh(); // TODO should not be necessary
        });
    }

    private void bind(KeyCode keyCode, Button button, int note, KeyboardKeyboard midiInOut) {
        LOGGER.debug("keyCodeToButton: <{}>", keyCodeToButton);
        keyCodeToButton.put(keyCode, button);
        button.armedProperty().addListener((observable, wasPressed, pressed) -> {
            midiInOut.keyChange(note, pressed);
        });
    }

}
