package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nl.nlcode.javafxutil.FxmlController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class ErrorUi extends BorderPane implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Label errorMessage;

    public ErrorUi(ResourceBundle bundle) {
        loadFxml(bundle);
    }
    
    public Label getErrorMessage() {
        return errorMessage;
    }

    public void close() {
        ((Stage) getScene().getWindow()).close();
    }
}
