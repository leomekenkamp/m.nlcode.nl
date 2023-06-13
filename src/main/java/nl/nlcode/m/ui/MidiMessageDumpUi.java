package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.MidiMessageDump;
import nl.nlcode.m.engine.MidiMessageDump.EnhancedMessage;
import nl.nlcode.m.engine.MidiMessageFormat;
import nl.nlcode.m.engine.ShowTicks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiMessageDumpUi extends MidiInOutUi<MidiMessageDump> implements MidiMessageDump.Ui, FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    @FXML
    private TableView midiMessageTable;

    @FXML
    private TableColumn messageReceivedAtColumn;

    @FXML
    private TableColumn messageHexColumn;

    @FXML
    private TableColumn descriptionColumn;

    @FXML
    private TableColumn timestampColumn;

    @FXML
    private EnumChoiceBox<ShowTicks> showTicks;

    public MidiMessageDumpUi(ProjectUi projectUi, MidiMessageDump midiMessageDump, MenuItem menuItem) {
        super(projectUi, midiMessageDump, menuItem);
        loadFxml(MidiMessageDumpUi.class, App.MESSAGES);

        messageReceivedAtColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MidiMessageDump.EnhancedMessage, StringProperty>, StringProperty>() {
            @Override
            public StringProperty call(TableColumn.CellDataFeatures<MidiMessageDump.EnhancedMessage, StringProperty> cell) {
                return new SimpleStringProperty(cell.getValue().receivedAt().format(MidiInOut.LOCAL_DATE_TIME));

            }
        });
        messageHexColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MidiMessageDump.EnhancedMessage, StringProperty>, StringProperty>() {
            @Override
            public StringProperty call(TableColumn.CellDataFeatures<MidiMessageDump.EnhancedMessage, StringProperty> cell) {
                return new SimpleStringProperty(MidiInOut.toHex(cell.getValue().rawSource(), 3));
            }
        });
        descriptionColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MidiMessageDump.EnhancedMessage, StringProperty>, StringProperty>() {
            @Override
            public StringProperty call(TableColumn.CellDataFeatures<MidiMessageDump.EnhancedMessage, StringProperty> cell) {
                return new SimpleStringProperty(cell.getValue().description());
            }
        });
        timestampColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MidiMessageDump.EnhancedMessage, LongProperty>, LongProperty>() {
            @Override
            public LongProperty call(TableColumn.CellDataFeatures<MidiMessageDump.EnhancedMessage, LongProperty> cell) {
                return new SimpleLongProperty(cell.getValue().timestamp());
            }
        });
//        messageColumn.prefWidthProperty().bind(midiMessageTable.widthProperty().multiply(0.3));
//        descriptionColumn.prefWidthProperty().bind(midiMessageTable.widthProperty().multiply(0.7));
        showTicks.valueProperty().addListener((ov, oldValue, newValue) -> {
            LOGGER.debug("setting showTicks to <{}>", newValue);
            getMidiInOut().setShowTicks(newValue);
        });
        showTicks.setValue(getMidiInOut().getShowTicks());
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        midiMessageTable.getItems().addAll(getMidiInOut().getEnhancedMessageList());
    }

    @Override
    public void received(EnhancedMessage messageAndTime) {
        Platform.runLater(() -> {
            midiMessageTable.getItems().add(0, messageAndTime);
            while (midiMessageTable.getItems().size() > 100) {
                midiMessageTable.getItems().remove(midiMessageTable.getItems().size() - 1);
            }
        });
    }

}
