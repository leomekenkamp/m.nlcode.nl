package nl.nlcode.m.ui;

import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javax.sound.midi.MidiMessage;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.MidiMessageDump;
import nl.nlcode.m.engine.MidiMessageDump.MessageAndTime;
import nl.nlcode.m.engine.MidiMessageFormat;
import nl.nlcode.m.engine.ShowTicks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiMessageDumpUi extends MidiInOutUi<MidiMessageDump> implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiMessageDumpUi.class);

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    @FXML
    private TableView midiMessageTable;

    @FXML
    private TableColumn messageReceivedAtColumn;

    @FXML
    private TableColumn messageHexColumn;

    @FXML
    private TableColumn messageDecColumn;

    @FXML
    private TableColumn descriptionColumn;

    @FXML
    private EnumChoiceBox<ShowTicks> showTicks;

    public MidiMessageDumpUi(ProjectUi projectUi, MidiMessageDump midiMessageDump, MenuItem menuItem) {
        super(projectUi, midiMessageDump, menuItem);
        loadFxml(MidiMessageDumpUi.class, App.MESSAGES);

        messageReceivedAtColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MidiMessageDump.MessageAndTime, StringProperty>, StringProperty>() {
            @Override
            public StringProperty call(TableColumn.CellDataFeatures<MidiMessageDump.MessageAndTime, StringProperty> cell) {
                return new SimpleStringProperty(cell.getValue().time.format(DateTimeFormatter.ISO_LOCAL_TIME));
            }
        });
        messageHexColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MidiMessageDump.MessageAndTime, StringProperty>, StringProperty>() {
            @Override
            public StringProperty call(TableColumn.CellDataFeatures<MidiMessageDump.MessageAndTime, StringProperty> cell) {
                return new SimpleStringProperty(toHex(cell.getValue().message, 3));
            }
        });
        messageDecColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MidiMessageDump.MessageAndTime, StringProperty>, StringProperty>() {
            @Override
            public StringProperty call(TableColumn.CellDataFeatures<MidiMessageDump.MessageAndTime, StringProperty> cell) {
                return new SimpleStringProperty(getMidiInOut().toDec(cell.getValue().message, 3));
            }
        });
        descriptionColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MidiMessageDump.MessageAndTime, StringProperty>, StringProperty>() {
            @Override
            public StringProperty call(TableColumn.CellDataFeatures<MidiMessageDump.MessageAndTime, StringProperty> cell) {
                return new SimpleStringProperty(MIDI_FORMAT.format(cell.getValue().message));
            }
        });
//        messageColumn.prefWidthProperty().bind(midiMessageTable.widthProperty().multiply(0.3));
//        descriptionColumn.prefWidthProperty().bind(midiMessageTable.widthProperty().multiply(0.7));
        midiMessageDump.getMidiMessageList().addListener(new ListChangeListener<>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends MidiMessageDump.MessageAndTime> change) {
                Platform.runLater(() -> {
                    synchronized (midiMessageDump.getMidiMessageList()) { // ugly as hell
                        while (change.next()) {
                            if ((change.wasRemoved())) {
                                // change.getRemoved() is buggy, use indices instead
                                for (int localIndex = change.getFrom(); localIndex <= change.getTo(); localIndex ++) {
                                    MessageAndTime removed = midiMessageDump.getMidiMessageList().get(localIndex);
                                    midiMessageTable.getItems().remove(removed);
                                }
                            }
                            if (change.wasAdded()) {
                                midiMessageTable.getItems().addAll(0, new ArrayList(change.getAddedSubList()));
                            }
                        }
                    }
                });
            }
        });
        showTicks.valueProperty().addListener((ov, oldValue, newValue) -> {
            LOGGER.debug("setting showTicks to <{}>", newValue);
            getMidiInOut().setShowTicks(newValue);
        });
        showTicks.setValue(getMidiInOut().getShowTicks());
    }

    @Override
    protected void doInit() {
        super.doInit();
    }

    private String toHex(MidiMessage msg, int maxLength) {
        StringJoiner result = new StringJoiner(" ");
        result.add(Integer.toHexString(msg.getStatus()));
        for (int i = 1; i < msg.getMessage().length; i++) {
            if (i >= maxLength) {
                result.add("\u2026 (+" + (msg.getMessage().length - maxLength) + ")");
                break;
            }
            result.add(Integer.toHexString(msg.getMessage()[i]));
        }
        return result.toString();
    }

}
