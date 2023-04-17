package nl.nlcode.m.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import nl.nlcode.m.engine.MidiChannelMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiChannelMatrixController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiChannelMatrixController.class);

    private static final int ROW_OFFSET = 1;

    private static final int COLUMN_OFFSET = 1;

    private CheckBox[][] checkBoxes = new CheckBox[16][16];
    
    @FXML
    private GridPane matrix;

    @FXML
    public void initialize() {
    }

    public void setParent(MidiChannelMatrixUi parent) {
        MidiChannelMatrix midiInOut = parent.getMidiInOut();

        for (int from = 0; from < 16; from++) {
            Label leftLabel = new Label("" + (from + 1));
            GridPane.setRowIndex(leftLabel, from + ROW_OFFSET);
            GridPane.setColumnIndex(leftLabel, 0);
            matrix.getChildren().add(leftLabel);

            Label topLabel = new Label("" + (from + 1));
            GridPane.setRowIndex(topLabel, 0);
            GridPane.setColumnIndex(topLabel, from + COLUMN_OFFSET);
            matrix.getChildren().add(topLabel);

            for (int to = 0; to < 16; to++) {
                CheckBox checkBox = new CheckBox();
                checkBoxes[from][to] = checkBox;
                GridPane.setRowIndex(checkBox, from + 1);
                GridPane.setColumnIndex(checkBox, to + 1);
                matrix.getChildren().add(checkBox);
                checkBox.setSelected(midiInOut.zeroBasedFromTo(from, to));
                checkBox.selectedProperty().addListener((ov, oldValue, newValue) -> {
                    midiInOut.zeroBasedFromTo(row(checkBox), column(checkBox), newValue);
                    parent.setDirty();
                });
            }
        }
        
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

    private int column(CheckBox checkBox) {
        return GridPane.getColumnIndex(checkBox) - COLUMN_OFFSET;
    }

    private int row(CheckBox checkBox) {
        return GridPane.getRowIndex(checkBox) - ROW_OFFSET;
    }

    @FXML
    private void reset(ActionEvent e) {
        for (int from = 0; from < checkBoxes.length; from++) {
            for (int to = 0; to < checkBoxes.length; to++) {
                checkBoxes[from][to].setSelected(from == to);
            }
        }
    }

    @FXML
    private void clear(ActionEvent e) {
        for (int from = 0; from < checkBoxes.length; from++) {
            for (int to = 0; to < checkBoxes.length; to++) {
                checkBoxes[from][to].setSelected(false);
            }
        }
    }

    @FXML
    private void invert(ActionEvent e) {
        for (int from = 0; from < checkBoxes.length; from++) {
            for (int to = 0; to < checkBoxes.length; to++) {
                checkBoxes[from][to].setSelected(!checkBoxes[from][to].isSelected());
            }
        }
    }

}
