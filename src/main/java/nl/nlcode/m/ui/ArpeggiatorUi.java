package nl.nlcode.m.ui;

import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import nl.nlcode.m.engine.Arpeggiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class ArpeggiatorUi extends MidiInOutUi<Arpeggiator> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArpeggiatorUi.class);

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

    @FXML
    private Spinner<Integer> channel;

    @FXML
    private Spinner<Integer> octave;

    @FXML
    private GridPane keyGrid;

    public ArpeggiatorUi(ProjectUi projectUi, Arpeggiator arpeggiator, MenuItem menuItem) {
        super(projectUi, arpeggiator, menuItem);
        LOGGER.debug("keyCodeToButton: <{}>", keyCodeToButton);
        loadFxml(App.MESSAGES);
    }

    @Override
    protected void doInit() {
        super.doInit();

        bind(KeyCode.A, a0, 57);
        bind(KeyCode.W, aSharp0, 58);
        bind(KeyCode.S, b0, 59);
        bind(KeyCode.D, c0, 60);
        bind(KeyCode.R, cSharp0, 61);
        bind(KeyCode.F, d0, 62);
        bind(KeyCode.T, dSharp0, 63);
        bind(KeyCode.G, e0, 64);
        bind(KeyCode.H, f0, 65);
        bind(KeyCode.U, fSharp0, 66);
        bind(KeyCode.J, g0, 67);
        bind(KeyCode.I, gSharp0, 68);
        bind(KeyCode.K, a1, 69);
        bind(KeyCode.O, aSharp1, 70);
        bind(KeyCode.L, b1, 71);
        bind(KeyCode.SEMICOLON, c1, 72);
        bind(KeyCode.OPEN_BRACKET, cSharp1, 73);
        bind(KeyCode.QUOTE, d1, 74);
        bind(KeyCode.CLOSE_BRACKET, dSharp1, 75);
        bind(KeyCode.BACK_SLASH, e1, 76);

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

//        velocity.getValueFactory().setValue(getMidiInOut().getVelocity());
//        velocity.valueProperty().addListener((ov, oldValue, newValue) -> {
//            getMidiInOut().setVelocity(newValue);
//            setDirty();
//        });
//        channel.getValueFactory().setValue(getMidiInOut().getOneBasedChannel());
//        channel.valueProperty().addListener((ov, oldValue, newValue) -> {
//            getMidiInOut().setOneBasedChannel(newValue);
//            setDirty();
//        });
//
//        octave.getValueFactory().setValue(getMidiInOut().getOctave());
//        octave.valueProperty().addListener((ov, oldValue, newValue) -> {
//            getMidiInOut().setOctave(newValue);
//            setDirty();
//        });

    }

    private void bind(KeyCode keyCode, Button button, int note) {
        LOGGER.debug("keyCodeToButton: <{}>", keyCodeToButton);
        keyCodeToButton.put(keyCode, button);
//        button.armedProperty().addListener((observable, wasPressed, pressed) -> {
//            getMidiInOut().keyChange(note, pressed);
//        });
    }

}
