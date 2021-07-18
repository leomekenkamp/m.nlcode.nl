package nl.nlcode.m.ui;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.ClockSource;
import nl.nlcode.m.engine.MidiClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiClockUi extends MidiInOutUi<MidiClock> implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiClockUi.class);

    private IntegerProperty barProperty = new SimpleIntegerProperty();
    private IntegerProperty beatProperty = new SimpleIntegerProperty();
    private IntegerProperty tickProperty = new SimpleIntegerProperty();

    @FXML
    private Spinner<Integer> beatsPerMinute;

    @FXML
    private Spinner<Integer> beatsPerBar;

    @FXML
    private EnumChoiceBox<ClockSource> clockChoice;

    public MidiClockUi(ProjectUi projectUi, MidiClock midiClock, MenuItem menuItem) {
        super(projectUi, midiClock, menuItem);
        loadFxml(MidiClockUi.class, App.MESSAGES);
        midiClock.setOnBarChange(bar -> Platform.runLater(() -> barProperty.set(bar)));
        midiClock.setOnBeatChange(beat -> Platform.runLater(() -> beatProperty.set(beat)));
        midiClock.setOnTickChange(tick -> Platform.runLater(() -> tickProperty.set(tick)));
        barProperty.set(midiClock.getBar());
        beatProperty.set(midiClock.getBeat());
        tickProperty.set(midiClock.getTick());
    }

    @Override
    protected void doInit() {
        super.doInit();
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
        clockSourceProperty().set(getMidiInOut().getClockSource());
        beatsPerMinute.getValueFactory().setValue(Math.round(getMidiInOut().getBeatsPerMinute()));
        beatsPerMinute.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setBeatsPerMinute(newValue);
        });
        beatsPerBar.getValueFactory().setValue(getMidiInOut().getBeatsPerBar());
        beatsPerBar.getValueFactory().valueProperty().addListener((ov, oldValue, newValue) -> {
            getMidiInOut().setBeatsPerBar(newValue);
        });
    }

    public ObjectProperty<ClockSource> clockSourceProperty() {
        return clockChoice.valueProperty();
    }

    public Integer getBar() {
        return barProperty.get();
    }

    public void setBar(Integer bar) {
        barProperty.set(bar);
    }

    public IntegerProperty barProperty() {
        return barProperty;
    }

    public Integer getBeat() {
        return beatProperty.get();
    }

    public void setBeat(Integer beat) {
        beatProperty.set(beat);
    }

    public IntegerProperty beatProperty() {
        return beatProperty;
    }

    public Integer getTick() {
        return tickProperty.get();
    }

    public void setTick(Integer tick) {
        tickProperty.set(tick);
    }

    public IntegerProperty tickProperty() {
        return tickProperty;
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
}
