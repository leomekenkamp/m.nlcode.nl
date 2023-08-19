package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import nl.nlcode.m.engine.Echo;
import nl.nlcode.m.engine.TickSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class EchoUi extends MidiInOutUi<Echo> implements Echo.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private EnumChoiceBox<TickSource> tickSource;
    private final ObjectPropertyUpdaterBridge<TickSource> tickSourceBackend;

    @FXML
    private Spinner<Integer> echoLength;
    private final IntPropertyUpdaterBridge echoLengthBackend;

    @FXML
    private Spinner<Integer> absoluteVelocityDecrease;
    private final IntPropertyUpdaterBridge absoluteVelocityDecreaseBackend;

    @FXML
    private Spinner<Integer> relativeVelocityDecrease;
    private final IntPropertyUpdaterBridge relativeVelocityDecreaseBackend;

    public EchoUi(ProjectUi projectUi, Echo echo, MenuItem menuItem) {
        super(projectUi, echo, menuItem);
        loadFxml(EchoUi.class, App.MESSAGES);
        tickSourceBackend = ObjectPropertyUpdaterBridge.create(getMidiInOut().tickSource(), tickSource.valueProperty());
        echoLengthBackend = IntPropertyUpdaterBridge.create(getMidiInOut().echoLength(), echoLength.getValueFactory().valueProperty());
        absoluteVelocityDecreaseBackend = IntPropertyUpdaterBridge.create(getMidiInOut().absoluteVelocityDecrease(), absoluteVelocityDecrease.getValueFactory().valueProperty());
        relativeVelocityDecreaseBackend = IntPropertyUpdaterBridge.create(getMidiInOut().relativeVelocityDecrease(), relativeVelocityDecrease.getValueFactory().valueProperty());
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        getReceivingFrom().addListener(new DebugListChangeListenener());

    }

}
