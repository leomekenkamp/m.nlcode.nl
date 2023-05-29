package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.m.engine.ProgramChanger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class ProgramChangerUi extends MidiInOutUi<ProgramChanger> implements ProgramChanger.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Spinner<Integer> programSpinner0;

    @FXML
    private Spinner<Integer> programSpinner1;

    @FXML
    private Spinner<Integer> programSpinner2;

    @FXML
    private Spinner<Integer> programSpinner3;

    @FXML
    private Spinner<Integer> programSpinner4;

    @FXML
    private Spinner<Integer> programSpinner5;

    @FXML
    private Spinner<Integer> programSpinner6;

    @FXML
    private Spinner<Integer> programSpinner7;

    @FXML
    private Spinner<Integer> programSpinner8;

    @FXML
    private Spinner<Integer> programSpinner9;

    @FXML
    private Spinner<Integer> programSpinner10;

    @FXML
    private Spinner<Integer> programSpinner11;

    @FXML
    private Spinner<Integer> programSpinner12;

    @FXML
    private Spinner<Integer> programSpinner13;

    @FXML
    private Spinner<Integer> programSpinner14;

    @FXML
    private Spinner<Integer> programSpinner15;

    @FXML
    private CheckBox resendOnConnect;

    @FXML
    private CheckBox resendOnMidiDeviceChange;

    private volatile Spinner<Integer>[] programSpinners;

    public ProgramChangerUi(ProjectUi projectUi, ProgramChanger programChanger, MenuItem menuItem) {
        super(projectUi, programChanger, menuItem);

        loadFxml(ProgramChangerUi.class, App.MESSAGES);
    }

    @FXML
    public void resend() {
        getMidiInOut().resend();
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();

        programSpinners = new Spinner[]{
            programSpinner0,
            programSpinner1,
            programSpinner2,
            programSpinner3,
            programSpinner4,
            programSpinner5,
            programSpinner6,
            programSpinner7,
            programSpinner8,
            programSpinner9,
            programSpinner10,
            programSpinner11,
            programSpinner12,
            programSpinner13,
            programSpinner14,
            programSpinner15,};

        forAllChannels(channel -> {
            programSpinners[channel].getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
                getMidiInOut().setProgram(channel, newValue);
            });
        });
        resendOnConnect.selectedProperty().addListener((ov, oldValue, newValue) -> getMidiInOut().autoSendOnConnect().set(newValue));
        resendOnMidiDeviceChange.selectedProperty().addListener((ov, oldValue, newValue) -> getMidiInOut().setAutoSendOnMidiDeviceChange(newValue));
    }

    @Override
    public void updateProgram(int channel, int program) {
        LOGGER.info("ch: {}; program: {}", channel, program);
        Platform.runLater(() -> {
            programSpinners[channel].getValueFactory().setValue(program);
        });
    }

    @Override
    public void updateAutoSendOnConnect(boolean newValue) {
        Platform.runLater(() -> {
            resendOnConnect.selectedProperty().set(newValue);
        });
    }

    @Override
    public void updateAutoSendOnMidiDeviceChange(boolean newValue) {
        Platform.runLater(() -> {
            resendOnMidiDeviceChange.selectedProperty().set(newValue);
        });
    }

    @Override
    public void updateDropIncomingChanges(int channel, boolean drop) {
        Platform.runLater(() -> {
        });
    }

}
