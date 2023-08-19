package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import nl.nlcode.m.engine.Arpeggiator;
import nl.nlcode.m.engine.ArpeggiatorLengthPer;
import nl.nlcode.m.engine.Direction;
import nl.nlcode.m.engine.TickSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class ArpeggiatorUi extends MidiInOutUi<Arpeggiator> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private EnumChoiceBox<TickSource> tickSource;
    private final ObjectPropertyUpdaterBridge<TickSource> tickSourceBackend;

    @FXML
    private Spinner<Integer> channel;
    private final IntPropertyUpdaterBridge channelBackend;

    @FXML
    private Spinner<Integer> octaveUp;
    private final IntPropertyUpdaterBridge octaveUpBackend;

    @FXML
    private Spinner<Integer> octaveDown;
    private final IntPropertyUpdaterBridge octaveDownBackend;

    @FXML
    private Spinner<Integer> length;
    private final IntPropertyUpdaterBridge lengthBackend;

    @FXML
    private EnumChoiceBox<ArpeggiatorLengthPer> lengthPer;
    private final ObjectPropertyUpdaterBridge<ArpeggiatorLengthPer> lengthPerBackend;

    @FXML
    private Spinner<Integer> overrideAttackVelocity;
    private final IntPropertyUpdaterBridge overrideAttackVelocityBackend;

    @FXML
    private Spinner<Integer> releaseVelocity;
    private final IntPropertyUpdaterBridge releaseVelocityBackend;
    
//    @FXML
//    private EnumChoiceBox<ArpeggiatorActionTakesEffect> newChordTakesEffect;
//    private final ObjectUpdatePropertyBridge<TickSource> newChordTakesEffectBackend;

    @FXML // FIXME: change to radio buttons
    private EnumChoiceBox<Direction> chordDirection;
    private final ObjectPropertyUpdaterBridge<Direction> chordDirectionBackend;

    @FXML
    private EnumChoiceBox<Direction> rangeDirection;
    private final ObjectPropertyUpdaterBridge<Direction> rangeDirectionBackend;
    
    public ArpeggiatorUi(ProjectUi projectUi, Arpeggiator arpeggiator, MenuItem menuItem) {
        super(projectUi, arpeggiator, menuItem);
        loadFxml(ArpeggiatorUi.class, App.MESSAGES);
        tickSourceBackend = ObjectPropertyUpdaterBridge.create(getMidiInOut().tickSource(), tickSource.valueProperty());
        channelBackend = IntPropertyUpdaterBridge.create(getMidiInOut().channelProperty(), channel.getValueFactory().valueProperty());
        octaveUpBackend = IntPropertyUpdaterBridge.create(getMidiInOut().octaveUpProperty(), octaveUp.getValueFactory().valueProperty());
        octaveDownBackend = IntPropertyUpdaterBridge.create(getMidiInOut().octaveDownProperty(), octaveDown.getValueFactory().valueProperty());
        lengthBackend = IntPropertyUpdaterBridge.create(getMidiInOut().lengthProperty(), length.getValueFactory().valueProperty());
        lengthPerBackend = ObjectPropertyUpdaterBridge.create(getMidiInOut().lengthPerProperty(), lengthPer.valueProperty());
        overrideAttackVelocityBackend = IntPropertyUpdaterBridge.create(getMidiInOut().overrideAttackVelocityProperty(), overrideAttackVelocity.getValueFactory().valueProperty());
        releaseVelocityBackend = IntPropertyUpdaterBridge.create(getMidiInOut().releaseVelocityProperty(), releaseVelocity.getValueFactory().valueProperty());
        //newChordTakesEffectBackend = ObjectUpdatePropertyBridge.create(getMidiInOut().newChordTakesEffectProperty(), newChordTakesEffect.valueProperty());
        chordDirectionBackend = ObjectPropertyUpdaterBridge.create(getMidiInOut().chordDirectionUpdater(), chordDirection.valueProperty());
        rangeDirectionBackend = ObjectPropertyUpdaterBridge.create(getMidiInOut().rangeDirectionUpdater(), rangeDirection.valueProperty());
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
    }
}
