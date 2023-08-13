package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import nl.nlcode.m.engine.Arpeggiator;
import nl.nlcode.m.engine.ArpeggiatorLengthPer;
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
    private final ObjectUpdatePropertyBridge<TickSource> tickSourceBackend;

    @FXML
    private Spinner<Integer> channel;
    private final IntUpdatePropertyBridge channelBackend;

    @FXML
    private Spinner<Integer> octaveUp;
    private final IntUpdatePropertyBridge octaveUpBackend;

    @FXML
    private Spinner<Integer> octaveDown;
    private final IntUpdatePropertyBridge octaveDownBackend;

    @FXML
    private Spinner<Integer> length;
    private final IntUpdatePropertyBridge lengthBackend;

    @FXML
    private EnumChoiceBox<ArpeggiatorLengthPer> lengthPer;
    private final ObjectUpdatePropertyBridge<ArpeggiatorLengthPer> lengthPerBackend;

    @FXML
    private Spinner<Integer> overrideAttackVelocity;
    private final IntUpdatePropertyBridge overrideAttackVelocityBackend;

    @FXML
    private Spinner<Integer> releaseVelocity;
    private final IntUpdatePropertyBridge releaseVelocityBackend;
    
//    @FXML
//    private EnumChoiceBox<ArpeggiatorActionTakesEffect> newChordTakesEffect;
//    private final ObjectUpdatePropertyBridge<TickSource> newChordTakesEffectBackend;

    @FXML
    private ToggleGroup directionChord;
    
    @FXML
    private Toggle directionChordUp;
    
    @FXML
    private Toggle directionChordDown;
    
    public ArpeggiatorUi(ProjectUi projectUi, Arpeggiator arpeggiator, MenuItem menuItem) {
        super(projectUi, arpeggiator, menuItem);
        loadFxml(ArpeggiatorUi.class, App.MESSAGES);
        tickSourceBackend = ObjectUpdatePropertyBridge.create(getMidiInOut().tickSource(), tickSource.valueProperty());
        channelBackend = IntUpdatePropertyBridge.create(getMidiInOut().channelProperty(), channel.getValueFactory().valueProperty());
        octaveUpBackend = IntUpdatePropertyBridge.create(getMidiInOut().octaveUpProperty(), octaveUp.getValueFactory().valueProperty());
        octaveDownBackend = IntUpdatePropertyBridge.create(getMidiInOut().octaveDownProperty(), octaveDown.getValueFactory().valueProperty());
        lengthBackend = IntUpdatePropertyBridge.create(getMidiInOut().lengthProperty(), length.getValueFactory().valueProperty());
        lengthPerBackend = ObjectUpdatePropertyBridge.create(getMidiInOut().lengthPerProperty(), lengthPer.valueProperty());
        overrideAttackVelocityBackend = IntUpdatePropertyBridge.create(getMidiInOut().overrideAttackVelocityProperty(), overrideAttackVelocity.getValueFactory().valueProperty());
        releaseVelocityBackend = IntUpdatePropertyBridge.create(getMidiInOut().releaseVelocityProperty(), releaseVelocity.getValueFactory().valueProperty());
        //newChordTakesEffectBackend = ObjectUpdatePropertyBridge.create(getMidiInOut().newChordTakesEffectProperty(), newChordTakesEffect.valueProperty());
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
    }
}
