package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.m.engine.NoteChannelSpreader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class NoteChannelSpreaderUi extends MidiInOutUi<NoteChannelSpreader> implements NoteChannelSpreader.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Spinner<Integer> inputChannel;
    private IntPropertyUpdaterBridge channelBackend;

    @FXML
    private CheckBox useAsOutput0;

    @FXML
    private CheckBox useAsOutput1;

    @FXML
    private CheckBox useAsOutput2;

    @FXML
    private CheckBox useAsOutput3;

    @FXML
    private CheckBox useAsOutput4;

    @FXML
    private CheckBox useAsOutput5;

    @FXML
    private CheckBox useAsOutput6;

    @FXML
    private CheckBox useAsOutput7;

    @FXML
    private CheckBox useAsOutput8;

    @FXML
    private CheckBox useAsOutput9;

    @FXML
    private CheckBox useAsOutput10;

    @FXML
    private CheckBox useAsOutput11;

    @FXML
    private CheckBox useAsOutput12;

    @FXML
    private CheckBox useAsOutput13;

    @FXML
    private CheckBox useAsOutput14;

    @FXML
    private CheckBox useAsOutput15;

    private volatile CheckBox[] useAsOutputs;

    public NoteChannelSpreaderUi(ProjectUi projectUi, NoteChannelSpreader noteChannelSpreader, MenuItem menuItem) {
        super(projectUi, noteChannelSpreader, menuItem);
        loadFxml(NoteChannelSpreaderUi.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        useAsOutputs = new CheckBox[]{
            useAsOutput0, useAsOutput1, useAsOutput2, useAsOutput3,
            useAsOutput4, useAsOutput5, useAsOutput6, useAsOutput7,
            useAsOutput8, useAsOutput9, useAsOutput10, useAsOutput11,
            useAsOutput12, useAsOutput13, useAsOutput14, useAsOutput15
        };
        forAllChannels(channel -> {
            useAsOutputs[channel].selectedProperty().set(getMidiInOut().isOutputChannel(channel));
            useAsOutputs[channel].selectedProperty().addListener((ov, oldValue, newValue)
                    -> getMidiInOut().setOutputChannel(channel, newValue));
            useAsOutputs[channel].textProperty().bind(getProjectUi().channelTextProperty()[channel]);
        });
        channelBackend = IntPropertyUpdaterBridge.create(getMidiInOut().inputChannel(), inputChannel.getValueFactory().valueProperty());
        inputChannel.getValueFactory().setConverter(getMidiChannelStringConverter());

        getMidiChannelStringConverter().offsetProperty().addListener((ov, oldValue, newValue) -> {
            channelBackend.refresh(); // TODO should not be necessary
        });

    }

}
