package nl.nlcode.m.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javax.sound.midi.MidiDevice;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.Control;
import static nl.nlcode.m.ui.App.DEFAULT_CSS_FILENAME;
import static nl.nlcode.m.ui.ControlUi.ALL_FILTER;

import nl.nlcode.m.engine.MidiDeviceMgr;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public final class SettingsUi extends TabPane implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsUi.class);

    private static final FileChooser.ExtensionFilter CSS_FILTER
            = new FileChooser.ExtensionFilter(App.MESSAGES.getString("css.files"), "*.css");
    @FXML
    private Pane midiDevicesPane;

    private final Control control;

    private final MidiDeviceMgr midiDeviceMgr;

    public SettingsUi(Control control, MidiDeviceMgr midiDeviceMgr) {
        this.control = control;
        this.midiDeviceMgr = midiDeviceMgr;
        loadFxml(App.MESSAGES);
    }

    public final void initialize() {
        doRefresh();
    }

    @FXML
    private void refreshMidiDevices() {
        doRefresh();
        getScene().getWindow().sizeToScene();
    }

    @FXML
    private void selectCssFile() throws MalformedURLException {
        FileChooser fileChooser = ProjectUi.projectChooser("selectCssFile", control.getProjectDirectory());
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().addAll(SettingsUi.CSS_FILTER, ControlUi.ALL_FILTER);
        fileChooser.setSelectedExtensionFilter(CSS_FILTER);

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            URL url = file.toURI().toURL();
            App.setStyleSheet(url.toExternalForm());
            replaceStyleSheetOnAllWindows();
        }
    }

    @FXML
    private void replaceStyleSheetOnAllWindows() {
        App.replaceStyleSheetOnAllWindows();
    }

    @FXML
    private void defaultCss() {
        App.setStyleSheet(App.DEFAULT_STYLE_SHEET);
        App.replaceStyleSheetOnAllWindows();
    }

    @FXML
    private void extractDefaultCss() throws IOException {
        FileChooser fileChooser = ProjectUi.projectChooser("saveDefaultCss", control.getProjectDirectory());
        fileChooser.getExtensionFilters().addAll(CSS_FILTER, ALL_FILTER);
        fileChooser.setSelectedExtensionFilter(CSS_FILTER);
        fileChooser.setInitialFileName(DEFAULT_CSS_FILENAME);
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            InputStream cssStream = App.class.getResourceAsStream(DEFAULT_CSS_FILENAME);
            Files.copy(cssStream, file.toPath());
        }
    }
    
    private void doRefresh() {
        MidiDeviceMgr.getInstance().refreshMidiDevices();
        midiDevicesPane.getChildren().clear();
        for (MidiDevice device : midiDeviceMgr.getMidiDevices()) {
            LOGGER.debug("device {}", device);
            ToggleSwitch deviceSwitch = new ToggleSwitch();
            Label deviceLabel = new Label(MidiDeviceMgr.getDisplayName(device), deviceSwitch);
            deviceSwitch.selectedProperty().set(device.isOpen());
            deviceSwitch.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        midiDeviceMgr.open(device);
                    } else {
                        midiDeviceMgr.close(device);
                    }
                    ControlUi.prefs(device).putBoolean(ControlUi.OPEN, newValue);
                }
            });
            deviceSwitch.setAlignment(Pos.CENTER_RIGHT);
            midiDevicesPane.getChildren().add(deviceLabel);
        }
    }

    public String getStyleSheet() {
        return App.getStyleSheet();
    }
}
