package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.ClockSource;
import nl.nlcode.m.engine.I18n;
import nl.nlcode.m.engine.MidiClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiClockUi extends MidiInOutUi<MidiClock> implements FxmlController, MidiClock.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Spinner<Double> beatsPerMinute;
    private DoubleProperty beatsPerMinuteBridge;

    @FXML
    private Spinner<Integer> beatsPerBar;
    private IntegerProperty beatsPerBarBridge;

    @FXML
    private EnumChoiceBox<ClockSource> clockChoice;

    @FXML
    private Label barLabel;

    @FXML
    private Label beatLabel;

    @FXML
    private Label tickLabel;

    public MidiClockUi(ProjectUi projectUi, MidiClock midiClock, MenuItem menuItem) {
        super(projectUi, midiClock, menuItem);
        loadFxml(MidiClockUi.class, I18n.msg());
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        clockSourceProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setClockSource(newValue);
            switch (newValue) {
                case INTERNAL:
                    beatsPerMinute.setDisable(false);
                    break;
                case EXTERNAL:
                    beatsPerMinute.setDisable(true);
                    break;
            }
        });

        clockSourceProperty().set(getMidiInOut().getClockSource()); // TODO bridge
        beatsPerMinute.editorProperty().get().setAlignment(Pos.CENTER_RIGHT);
        beatsPerMinute.getValueFactory().setConverter(ControlUi.BPM_CONVERTER);
        beatsPerMinute.getValueFactory().setValue(getMidiInOut().getBeatsPerMinute());
        beatsPerMinute.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setBeatsPerMinute(newValue);
        });
        beatsPerMinuteBridge = DoublePropertyUpdaterBridge.create(getMidiInOut().beatsPerMinute(), beatsPerMinute.getValueFactory().valueProperty());

        beatsPerBar.editorProperty().get().setAlignment(Pos.CENTER_RIGHT);
        beatsPerBar.getValueFactory().setValue(getMidiInOut().getBeatsPerBar());
        beatsPerBar.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setBeatsPerBar(newValue);
        });
        beatsPerBarBridge = IntPropertyUpdaterBridge.create(getMidiInOut().beatsPerBar(), beatsPerBar.getValueFactory().valueProperty());
    }

    public ObjectProperty<ClockSource> clockSourceProperty() {
        return clockChoice.valueProperty();
    }

    @FXML
    private void start() {
        getMidiInOut().start();
    }

    @FXML
    private void stop() {
        getMidiInOut().stop();
    }

    @FXML
    private void resume() {
        getMidiInOut().resume();
    }

    @Override
    public void timingClock() {
        Platform.runLater(() -> tickLabel.textProperty().set(Integer.toString(getMidiInOut().getTick())));
    }

    @Override
    public void beatChanged() {
        Platform.runLater(() -> beatLabel.textProperty().set(Integer.toString(getMidiInOut().getBeat())));
    }

    @Override
    public void barChanged() {
        Platform.runLater(() -> barLabel.textProperty().set(Integer.toString(getMidiInOut().getBar())));
    }

    @Override
    public void bpmChanged() {
    }

}
