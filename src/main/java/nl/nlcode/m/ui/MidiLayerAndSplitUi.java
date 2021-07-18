package nl.nlcode.m.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import nl.nlcode.m.engine.MidiLayerAndSplit;
import nl.nlcode.m.engine.MidiLayerAndSplit.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiLayerAndSplitUi extends MidiInOutUi<MidiLayerAndSplit> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiLayerAndSplitUi.class);

    public static class LayerUi {

        private MidiLayerAndSplit.Layer layer;

        private IntegerProperty fromNote;

        private IntegerProperty toNote;

        private IntegerProperty inputChannel;

        private IntegerProperty outputChannel;

        private IntegerProperty transpose;

        public LayerUi(MidiLayerAndSplit.Layer layer) {
            try {
                this.layer = layer;
                fromNote = JavaBeanIntegerPropertyBuilder.create().bean(layer).name("fromNote").build();
                toNote = JavaBeanIntegerPropertyBuilder.create().bean(layer).name("toNote").build();
                inputChannel = JavaBeanIntegerPropertyBuilder.create().bean(layer).name("inputChannelOneBased").build();
                outputChannel = JavaBeanIntegerPropertyBuilder.create().bean(layer).name("outputChannelOneBased").build();
                transpose = JavaBeanIntegerPropertyBuilder.create().bean(layer).name("transpose").build();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
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

        public IntegerProperty InputChannelProperty() {
            return inputChannel;
        }

        public int getInputChannel() {
            return inputChannel.get();
        }

        public void setInputChannel(int channel) {
            inputChannel.set(channel);
        }

        public IntegerProperty OutputChannelProperty() {
            return outputChannel;
        }

        public int getOutputChannel() {
            return outputChannel.get();
        }

        public void setOutputChannel(int channel) {
            outputChannel.set(channel);
        }

        public IntegerProperty TransposeProperty() {
            return transpose;
        }

        public int getTranspose() {
            return transpose.get();
        }

        public void setTranspose(int keys) {
            transpose.set(keys);
        }

        public Layer getLayer() {
            return layer;
        }
    }

    @FXML
    private TableView<MidiLayerAndSplitUi.LayerUi> layersView;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button removeButton;

    @FXML
    private TableColumn inputChannelColumn;

    @FXML
    private TableColumn fromNoteColumn;

    @FXML
    private TableColumn toNoteColumn;

    @FXML
    private TableColumn transposeColumn;

    @FXML
    private TableColumn outputChannelColumn;

    public MidiLayerAndSplitUi(ProjectUi projectUi, MidiLayerAndSplit midiLayerAndSplit, MenuItem menuItem) {
        super(projectUi, midiLayerAndSplit, menuItem);
        loadFxml(App.MESSAGES);
        layersView.setEditable(true);
        for (Layer layer : midiLayerAndSplit.getLayers()) {
            layersView.getItems().add(new LayerUi(layer));
        }
        editButton.disableProperty().bind(Bindings.isEmpty(layersView.getSelectionModel().getSelectedItems()));
        editButton.setOnAction(eh -> {
            TablePosition pos = layersView.getFocusModel().getFocusedCell();
            layersView.edit(pos.getRow(), pos.getTableColumn());
        });

        addButton.setOnAction(eh -> {
            layersView.getItems().add(new LayerUi(getMidiInOut().createLayer()));
            setDirty();
        });

        removeButton.disableProperty().bind(Bindings.isEmpty(layersView.getSelectionModel().getSelectedItems()));
        removeButton.setOnAction(eh -> {
            List<LayerUi> remove = new ArrayList<>(layersView.getSelectionModel().getSelectedItems());
            // intermediary step needed because getSelectedItems does not support removal
            for (LayerUi layerUi : remove) {
                layersView.getItems().remove(layerUi);
                layerUi.getLayer().remove();
            }
            setDirty();
        });

        inputChannelColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("inputChannel"));
        inputChannelColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        inputChannelColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<LayerUi, Integer>>() {
            @Override
            public void handle(CellEditEvent<LayerUi, Integer> t) {
                ((LayerUi) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setInputChannel(t.getNewValue());
                setDirty();
            }
        });
        fromNoteColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("fromNote"));
        fromNoteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        fromNoteColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<LayerUi, Integer>>() {
            @Override
            public void handle(CellEditEvent<LayerUi, Integer> t) {
                ((LayerUi) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setFromNote(t.getNewValue());
                setDirty();
            }
        });
        toNoteColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("toNote"));
        toNoteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        toNoteColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<LayerUi, Integer>>() {
            @Override
            public void handle(CellEditEvent<LayerUi, Integer> t) {
                ((LayerUi) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setToNote(t.getNewValue());
                setDirty();
            }
        });
        transposeColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("transpose"));
        transposeColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        transposeColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<LayerUi, Integer>>() {
            @Override
            public void handle(CellEditEvent<LayerUi, Integer> t) {
                ((LayerUi) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setTranspose(t.getNewValue());
                setDirty();
            }
        });
        outputChannelColumn.setCellValueFactory(new PropertyValueFactory<LayerUi, Integer>("outputChannel"));
        outputChannelColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        outputChannelColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<LayerUi, Integer>>() {
            @Override
            public void handle(CellEditEvent<LayerUi, Integer> t) {
                ((LayerUi) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setOutputChannel(t.getNewValue());
                setDirty();
            }
        });
    }

}
