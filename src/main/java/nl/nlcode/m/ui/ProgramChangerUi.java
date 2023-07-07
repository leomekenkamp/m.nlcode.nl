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
    private Field channel0Field, channel1Field, channel2Field, channel3Field,
            channel4Field, channel5Field, channel6Field, channel7Field,
            channel8Field, channel9Field, channel10Field, channel11Field,
            channel12Field, channel13Field, channel14Field, channel15Field;

    @FXML
    private Spinner<Integer> programSpinner0, programSpinner1, programSpinner2, programSpinner3,
            programSpinner4, programSpinner5, programSpinner6, programSpinner7,
            programSpinner8, programSpinner9, programSpinner10, programSpinner11,
            programSpinner12, programSpinner13, programSpinner14, programSpinner15;

    @FXML
    private CheckBox dropIncoming0, dropIncoming1, dropIncoming2, dropIncoming3,
            dropIncoming4, dropIncoming5, dropIncoming6, dropIncoming7,
            dropIncoming8, dropIncoming9, dropIncoming10, dropIncoming11,
            dropIncoming12, dropIncoming13, dropIncoming14, dropIncoming15;

    @FXML
    private CheckBox resendOnConnect;

    @FXML
    private CheckBox resendOnMidiDeviceChange;

    private volatile Spinner<Integer>[] programSpinners;
    private volatile CheckBox[] dropIncomings;

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
            programSpinner0, programSpinner1, programSpinner2, programSpinner3,
            programSpinner4, programSpinner5, programSpinner6, programSpinner7,
            programSpinner8, programSpinner9, programSpinner10, programSpinner11,
            programSpinner12, programSpinner13, programSpinner14, programSpinner15
        };
        forAllChannels(channel -> {
            programSpinners[channel].getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
                getMidiInOut().setProgram(channel, newValue);
            });
        });

        dropIncomings = new CheckBox[]{
            dropIncoming0, dropIncoming1, dropIncoming2, dropIncoming3,
            dropIncoming4, dropIncoming5, dropIncoming6, dropIncoming7,
            dropIncoming8, dropIncoming9, dropIncoming10, dropIncoming11,
            dropIncoming12, dropIncoming13, dropIncoming14, dropIncoming15
        };
        forAllChannels(channel -> {
            dropIncomings[channel].selectedProperty().addListener((ov, oldValue, newValue)
                    -> getMidiInOut().setDropIncomingChanges(channel, newValue));
        });

        resendOnConnect.selectedProperty().addListener((ov, oldValue, newValue) -> getMidiInOut().setResendOnConnect(newValue));
        resendOnMidiDeviceChange.selectedProperty().addListener((ov, oldValue, newValue) -> getMidiInOut().setResendOnMidiDeviceChange(newValue));
        
        Field[] channelFields = new Field[]{
            channel0Field, channel1Field, channel2Field, channel3Field,
            channel4Field, channel5Field, channel6Field, channel7Field,
            channel8Field, channel9Field, channel10Field, channel11Field,
            channel12Field, channel13Field, channel14Field, channel15Field
        };
        forAllChannels(channel -> channelFields[channel].labelTextProperty().bind(getProjectUi().channelTextProperty()[channel]));
    }

    @Override
    public void updateProgram(int channel, int program) {
        Platform.runLater(() -> {
            programSpinners[channel].getValueFactory().setValue(program);
        });
    }

    @Override
    public void updateResendOnConnect(boolean newValue) {
        Platform.runLater(() -> {
            resendOnConnect.selectedProperty().set(newValue);
        });
    }

    @Override
    public void updateResendOnMidiDeviceChange(boolean newValue) {
        Platform.runLater(() -> {
            resendOnMidiDeviceChange.selectedProperty().set(newValue);
        });
    }

    @Override
    public void updateDropIncomingChanges(int channel, boolean drop) {
        Platform.runLater(() -> {
            dropIncomings[channel].selectedProperty().setValue(drop);
        });
    }

}
