package nl.nlcode.m.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
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
import nl.nlcode.m.engine.I18n;
import static nl.nlcode.m.ui.FxApp.DEFAULT_CSS_FILENAME;
import static nl.nlcode.m.ui.ControlUi.ALL_FILTER;

import nl.nlcode.m.engine.MidiDeviceMgr;
import nl.nlcode.m.engine.SaveFileEncoding;
import static nl.nlcode.m.ui.ZeroOrOneBased.ONE_BASED;
import static nl.nlcode.m.ui.ZeroOrOneBased.ZERO_BASED;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public final class SettingsUi extends TabPane implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final FileChooser.ExtensionFilter CSS_FILTER
            = new FileChooser.ExtensionFilter(I18n.msg().getString("css.files"), "*.css");
    @FXML
    private Pane midiDevicesPane;

    private final ControlUi controlUi;

    @FXML
    private EnumChoiceBox<ZeroOrOneBased> noteZeroOne;

    @FXML
    private EnumChoiceBox<ZeroOrOneBased> channelZeroOne;

    @FXML
    private EnumChoiceBox<NoteNamingConvention> noteNamingConvention;
    
    @FXML
    private EnumChoiceBox<SaveFileEncoding> saveFileEncoding;
    
    // SceneBuider use ONLY
    public SettingsUi() {
        this(ControlUi.getInstance(Control.getInstance()));
    }
    
    public SettingsUi(ControlUi controlUi) {
        this.controlUi = controlUi;
        loadFxml(I18n.msg());
    }

    private Control getControl() {
        return getControlUi().getControl();
    }

    private ControlUi getControlUi() {
        return controlUi;
    }

    public final void initialize() {
        doRefresh();
        noteZeroOne.valueProperty().setValue(ZeroOrOneBased.getZeroBased(getControlUi().getMidiNoteZeroBased()));
        noteZeroOne.valueProperty().addListener((ov, oldValue, newValue) -> {
            switch (newValue) {
                case ZERO_BASED:
                    getControlUi().setMidiNoteZeroBased(true);
                    break;
                case ONE_BASED:
                    getControlUi().setMidiNoteZeroBased(false);
                    break;
            }
        });
        channelZeroOne.valueProperty().setValue(ZeroOrOneBased.getZeroBased(getControlUi().getMidiChannelZeroBased()));
        channelZeroOne.valueProperty().addListener((ov, oldValue, newValue) -> {
            switch (newValue) {
                case ZERO_BASED:
                    controlUi.setMidiChannelZeroBased(true);
                    break;
                case ONE_BASED:
                    controlUi.setMidiChannelZeroBased(false);
                    break;
            }
        });
        noteNamingConvention.valueProperty().setValue(getControlUi().getNoteNamingConvention());
        noteNamingConvention.valueProperty().addListener((ov, oldValue, newValue) -> getControlUi().setNoteNamingConvention(newValue));
        saveFileEncoding.setValue(getControl().getSaveFileEncoding());
        saveFileEncoding.valueProperty().addListener((ov, oldValue, newValue) -> getControl().setSaveFileEncoding(newValue));
    }

    @FXML
    private void refreshMidiDevices() {
        doRefresh();
        getScene().getWindow().sizeToScene();
    }

    @FXML
    private void selectCssFile() throws MalformedURLException {
        FileChooser fileChooser = ProjectUi.projectChooser("selectCssFile", getControl().getProjectDirectory());
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().addAll(SettingsUi.CSS_FILTER, ControlUi.ALL_FILTER);
        fileChooser.setSelectedExtensionFilter(CSS_FILTER);

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            URL url = file.toURI().toURL();
            FxApp.setStyleSheet(url.toExternalForm());
            replaceStyleSheetOnAllWindows();
        }
    }

    @FXML
    private void replaceStyleSheetOnAllWindows() {
        FxApp.replaceStyleSheetOnAllWindows();
    }

    @FXML
    private void defaultCss() {
        FxApp.setStyleSheet(FxApp.DEFAULT_STYLE_SHEET);
        FxApp.replaceStyleSheetOnAllWindows();
    }

    @FXML
    private void extractDefaultCss() throws IOException {
        FileChooser fileChooser = ProjectUi.projectChooser("saveDefaultCss", getControl().getProjectDirectory());
        fileChooser.getExtensionFilters().addAll(CSS_FILTER, ALL_FILTER);
        fileChooser.setSelectedExtensionFilter(CSS_FILTER);
        fileChooser.setInitialFileName(DEFAULT_CSS_FILENAME);
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            InputStream cssStream = FxApp.class.getResourceAsStream(DEFAULT_CSS_FILENAME);
            Files.copy(cssStream, file.toPath());
        }
    }

    private void doRefresh() {
        MidiDeviceMgr midiDeviceMgr = getControlUi().getMidiDeviceMgr();
        midiDeviceMgr.refreshMidiDevices();
        midiDevicesPane.getChildren().clear();
        for (MidiDevice device : getControlUi().getMidiDeviceMgr().getMidiDevices()) {
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
                    getControl().getPreferences(device).putBoolean(Control.OPEN, newValue);
                }
            });
            deviceSwitch.setAlignment(Pos.CENTER_RIGHT);
            midiDevicesPane.getChildren().add(deviceLabel);
        }
    }

    public String getStyleSheet() {
        return FxApp.getStyleSheet();
    }

}
