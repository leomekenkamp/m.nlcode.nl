package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import nl.nlcode.m.engine.LayerAndSplit;
import nl.nlcode.m.engine.LayerAndSplit.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class LayerAndSplitUi extends MidiInOutUi<LayerAndSplit> implements LayerAndSplit.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void layerAdded(Layer layer) {
        layersView.getItems().add(new LayerUi(layer));
    }

    @Override
    public void layerRemoved(Layer layer) {
        for (LayerUi layerUi : layersView.getItems()) {
            if (layerUi.getLayer() == layer) {
                layersView.getItems().remove(layerUi);
                break;
            }
        }
    }

    public class LayerUi {

        private LayerAndSplit.Layer layer;

        private IntegerProperty fromNote;

        private IntegerProperty toNote;

        private IntegerProperty inputChannel;

        private IntegerProperty outputChannel;

        private IntegerProperty transpose;

        public LayerUi(LayerAndSplit.Layer layer) {
            this.layer = layer;
            fromNote = IntPropertyUpdaterBridge.create(layer.fromNote());
            toNote = IntPropertyUpdaterBridge.create(layer.toNote());
            inputChannel = IntPropertyUpdaterBridge.create(layer.inputChannel());
            outputChannel = IntPropertyUpdaterBridge.create(layer.outputChannel());
            transpose = IntPropertyUpdaterBridge.create(layer.transpose());
        }

        public IntegerProperty fromNoteProperty() {
            return fromNote;
        }

        public int getFromNote() {
            return fromNote.get();
        }

        public void setFromNote(int from) {
            fromNote.set(from);
        }

        public IntegerProperty toNoteProperty() {
            return toNote;
        }

        public int getToNote() {
            return toNote.get();
        }

        public void setToNote(int to) {
            toNote.set(to);
        }

        public IntegerProperty inputChannelProperty() {
            return inputChannel;
        }

        public int getInputChannel() {
            return inputChannel.get();
        }

        public void setInputChannel(int channel) {
            inputChannel.set(channel);
        }

        public IntegerProperty outputChannelProperty() {
            return outputChannel;
        }

        public int getOutputChannel() {
            return outputChannel.get();
        }

        public void setOutputChannel(int channel) {
            outputChannel.set(channel);
        }

        public IntegerProperty transposeProperty() {
            return transpose;
        }

        public int getTranspose() {
            return transpose.get();
        }

        public void setTranspose(int semiNotes) {
            transpose.set(semiNotes);
        }

        public Layer getLayer() {
            return layer;
        }
    }

    @FXML
    private TableView<LayerAndSplitUi.LayerUi> layersView;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button removeButton;

    @FXML
    private TableColumn inputChannelColumn;

    @FXML
    private TableColumn fromNoteNumberColumn;

    @FXML
    private TableColumn fromNoteNameColumn;

    @FXML
    private TableColumn toNoteNumberColumn;

    @FXML
    private TableColumn toNoteNameColumn;

    @FXML
    private TableColumn transposeColumn;

    @FXML
    private TableColumn outputChannelColumn;

    private ChangeListener<Integer> midiChannelStringRepresentationChanged = (ov, oldValue, newValue) -> {
        inputChannelColumn.getTableView().refresh();
    };


    public LayerAndSplitUi(ProjectUi projectUi, LayerAndSplit layerAndSplit, MenuItem menuItem) {
        super(projectUi, layerAndSplit, menuItem);
        loadFxml(App.MESSAGES);
        layersView.setEditable(true);
        for (Layer layer : (List<Layer>) layerAndSplit.getLayers()) { // TODO: why tf is a cast needed?
            layersView.getItems().add(new LayerUi(layer));
        }
        editButton.disableProperty().bind(Bindings.isEmpty(layersView.getSelectionModel().getSelectedItems()));
        editButton.setOnAction(eh -> {
            TablePosition pos = layersView.getFocusModel().getFocusedCell();
            layersView.edit(pos.getRow(), pos.getTableColumn());
        });

        addButton.setOnAction(eh -> {
            getMidiInOut().createLayer();
        });

        removeButton.disableProperty().bind(Bindings.isEmpty(layersView.getSelectionModel().getSelectedItems()));
        removeButton.setOnAction(eh -> {
            List<LayerUi> remove = new ArrayList<>(layersView.getSelectionModel().getSelectedItems());
            remove.forEach(layerUi -> {
                getMidiInOut().remove(layerUi.getLayer());
            });
            setDirty();
        });

        inputChannelColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("inputChannel"));
        inputChannelColumn.setCellFactory(TextFieldTableCell.forTableColumn(getMidiChannelStringConverter()));

        fromNoteNumberColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("fromNote"));
        fromNoteNumberColumn.setCellFactory(TextFieldTableCell.forTableColumn(getMidiNoteNumberStringConverter()));

        fromNoteNameColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("fromNote"));
        fromNoteNameColumn.setCellFactory(TextFieldTableCell.forTableColumn(getMidiNoteNameStringConverter()));

        toNoteNumberColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("toNote"));
        toNoteNumberColumn.setCellFactory(TextFieldTableCell.forTableColumn(getMidiNoteNumberStringConverter()));

        toNoteNameColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, String>("toNote"));
        toNoteNameColumn.setCellFactory(TextFieldTableCell.forTableColumn(getMidiNoteNameStringConverter()));

        transposeColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("transpose"));
        transposeColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        outputChannelColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("outputChannel"));
        outputChannelColumn.setCellFactory(TextFieldTableCell.forTableColumn(getMidiChannelStringConverter()));

        TableView table = inputChannelColumn.getTableView();
        getMidiChannelStringConverter().offsetProperty().addListener(new WeakChangeListener(midiChannelStringRepresentationChanged));
        getMidiNoteNumberStringConverter().offsetProperty().addListener(new WeakChangeListener(midiChannelStringRepresentationChanged));
        getMidiNoteNameStringConverter().noteNamingConvention().addListener(new WeakChangeListener(midiChannelStringRepresentationChanged));
    }

}
