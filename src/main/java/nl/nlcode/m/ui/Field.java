package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class Field extends VBox implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Label label;

    private StringProperty labelTextProperty;

    public Field() {
        loadFxml(I18n.msg());
        labelTextProperty = new SimpleStringProperty();
    }

    public StringProperty labelTextProperty() {
        return label.textProperty();
    }

    public String getLabelText() {
        return label.getText();
    }

    public void setLabelText(String labelText) {
        if (labelText != null) {
            for (;;) {
                int first = labelText.indexOf("__");
                if (first == -1) {
                    break;
                }
                int second = labelText.indexOf("__", first + 1);
                if (second == -1) {
                    break;
                }
                String i18key = labelText.substring(first + 2, second);
                String value = I18n.msg().getString(i18key);
                labelText = labelText.substring(0, first) + value + labelText.substring(second + 2, labelText.length());
            }
        }
        label.setText (labelText);
    }
}
