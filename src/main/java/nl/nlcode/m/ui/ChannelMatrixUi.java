package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import nl.nlcode.m.engine.ChannelMatrix;
import static nl.nlcode.m.engine.MidiInOut.CHANNEL_COUNT;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class ChannelMatrixUi extends MidiInOutUi<ChannelMatrix> implements ChannelMatrix.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int ROW_OFFSET = 1;

    private static final int COLUMN_OFFSET = 1;

    private CheckBox[][] checkBoxes = new CheckBox[CHANNEL_COUNT][CHANNEL_COUNT];

    @FXML
    private GridPane matrix;

    public ChannelMatrixUi(ProjectUi projectUi, ChannelMatrix channelMatrix, MenuItem menuItem) {
        super(projectUi, channelMatrix, menuItem);
        loadFxml(ChannelMatrixUi.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();

        forAllChannels(from -> {
            Label leftLabel = new Label();
            IntegerProperty offsetProperty = getProjectUi().getControlUi().getMidiChannelStringConverter().offsetProperty();
            StringBinding channelBinding = offsetProperty.add(from).asString();
            leftLabel.textProperty().bind(channelBinding);
            
            GridPane.setRowIndex(leftLabel, from + ROW_OFFSET);
            GridPane.setColumnIndex(leftLabel, 0);
            matrix.getChildren().add(leftLabel);

            Label topLabel = new Label(); 
            topLabel.textProperty().bind(channelBinding);
            
            GridPane.setRowIndex(topLabel, 0);
            GridPane.setColumnIndex(topLabel, from + COLUMN_OFFSET);
            matrix.getChildren().add(topLabel);

            forAllChannels(to -> {
                CheckBox checkBox = new CheckBox();
                checkBoxes[from][to] = checkBox;
                GridPane.setRowIndex(checkBox, from + 1);
                GridPane.setColumnIndex(checkBox, to + 1);
                matrix.getChildren().add(checkBox);
                checkBox.setSelected(getMidiInOut().getFromTo(from, to));
                checkBox.selectedProperty().addListener((ov, oldValue, newValue) -> {
                    getMidiInOut().setFromTo(row(checkBox), column(checkBox), newValue);
                });
            });
        });

//        // This is for when to put reverse, clear, reset buttons on each row (and column)
//        for (int i = 0; i < 16; i++) {
//            {
//                Button rowButton = new Button();
//                rowButton.setText("r");
//                rowButton.setStyle("-fx-font-size:9");
//
//                matrix.getChildren().add(rowButton);
//                GridPane.setRowIndex(rowButton, i + ROW_OFFSET);
//                GridPane.setColumnIndex(rowButton, 18);
//            }
//        }
    }

    @Override
    public void matrixChanged(int from, int to, boolean value) {
        checkBoxes[from][to].setSelected(value);
    }

    private int column(CheckBox checkBox) {
        return GridPane.getColumnIndex(checkBox) - COLUMN_OFFSET;
    }

    private int row(CheckBox checkBox) {
        return GridPane.getRowIndex(checkBox) - ROW_OFFSET;
    }

    @FXML
    private void reset(ActionEvent e) {
        forAllChannels(from -> forAllChannels(to -> checkBoxes[from][to].setSelected(from == to)));
    }

    @FXML
    private void clear(ActionEvent e) {
        forAllChannels(from -> forAllChannels(to -> checkBoxes[from][to].setSelected(false)));
    }

    @FXML
    private void invert(ActionEvent e) {
        forAllChannels(from -> forAllChannels(to -> checkBoxes[from][to].setSelected(!checkBoxes[from][to].isSelected())));
    }

}
