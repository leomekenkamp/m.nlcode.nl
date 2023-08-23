package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import nl.nlcode.m.engine.Example;
import static nl.nlcode.m.engine.MidiInOut.CHANNEL_COUNT;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.m.engine.Transposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class TransposerUi extends MidiInOutUi<Transposer> implements Example.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Spinner transposeOctaves;
    private IntPropertyUpdaterBridge transposeOctavesBackend;
            
    @FXML
    private Spinner transposeSemitones;
    private IntPropertyUpdaterBridge transposeSemitonesBackend;
            
    @FXML
    private CheckBox transposeChannel0, transposeChannel1, transposeChannel2, transposeChannel3,
            transposeChannel4, transposeChannel5, transposeChannel6, transposeChannel7,
            transposeChannel8, transposeChannel9, transposeChannel10, transposeChannel11,
            transposeChannel12, transposeChannel13, transposeChannel14, transposeChannel15;
    private BooleanPropertyUpdaterBridge[] transposeChannelUpdaterBridge;

    public TransposerUi(ProjectUi projectUi, Transposer transposer, MenuItem menuItem) {
        super(projectUi, transposer, menuItem);
        loadFxml(TransposerUi.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        transposeOctavesBackend = IntPropertyUpdaterBridge.create(getMidiInOut().octavesUpdater(), transposeOctaves.getValueFactory().valueProperty());
        transposeSemitonesBackend = IntPropertyUpdaterBridge.create(getMidiInOut().semitonesUpdater(), transposeSemitones.getValueFactory().valueProperty());
        CheckBox[] transposeChannels = new CheckBox[]{
            transposeChannel0, transposeChannel1, transposeChannel2, transposeChannel3,
            transposeChannel4, transposeChannel5, transposeChannel6, transposeChannel7,
            transposeChannel8, transposeChannel9, transposeChannel10, transposeChannel11,
            transposeChannel12, transposeChannel13, transposeChannel14, transposeChannel15
        };
        transposeChannelUpdaterBridge = new BooleanPropertyUpdaterBridge[CHANNEL_COUNT];
        forAllChannels(channel -> {
            transposeChannels[channel].textProperty().bind(getProjectUi().channelNumberProperty()[channel]);
            transposeChannelUpdaterBridge[channel] = BooleanPropertyUpdaterBridge.create(getMidiInOut().channelUpdater(channel), transposeChannels[channel].selectedProperty());
        });
    }

}
