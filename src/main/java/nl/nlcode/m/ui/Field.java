package nl.nlcode.m.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nl.nlcode.javafxutil.FxmlController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class Field extends VBox implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Field.class);

    @FXML
    private Label label;
    
    private StringProperty labelTextProperty;

    public Field() {
        loadFxml(App.MESSAGES);
        labelTextProperty = new SimpleStringProperty();
    }

    public StringProperty labelTextProperty() {
        return label.textProperty();
    }
    
    public String getLabelText() {
        return label.getText();
    }
    
    public void setLabelText(String labelText) {
        label.setText(labelText);
    }
}
