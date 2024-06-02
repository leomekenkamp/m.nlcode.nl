package nl.nlcode.m.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import javax.sound.midi.MidiDevice;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.Props;
import nl.nlcode.m.engine.Control;
import nl.nlcode.m.engine.MidiDeviceMgr;
import static nl.nlcode.m.engine.MidiDeviceMgr.COMPARE_BY_DISPLAY_NAME;
import nl.nlcode.m.engine.Project;
import static nl.nlcode.m.ui.ProjectUi.M_FILTER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main window.
 *
 * @author leo
 */
public class ControlUi extends BorderPane implements FxmlController, MidiDeviceMgr.Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final FileChooser.ExtensionFilter ALL_FILTER
            = new FileChooser.ExtensionFilter(App.MESSAGES.getString("allFiles"), "*.*");

    public static final FileChooser.ExtensionFilter CSS_FILTER
            = new FileChooser.ExtensionFilter(App.MESSAGES.getString("cssFiles"), "*.css");

    public static final String OPEN = "open";

    public static final int TABLE_VIEW_PREF_HEIGHT = 150;

    private static final Preferences systemMidiPrefs = Control.PREFERENCES.node("systemMidi");

    private static final Preferences openProjectsPrefs = Control.PREFERENCES.node("openProjects");

    public static final StringConverter<Double> BPM_CONVERTER = new StringConverter<Double>() {
        private final String format = "###.00";

        @Override
        public String toString(Double object) {
            if (object == null) {
                return "";
            }
            return new DecimalFormat(format).format(object);
        }

        @Override
        public Double fromString(String string) {
            try {
                if (string == null) {
                    return null;
                }
                string = string.trim();
                if (string.length() < 1) {
                    return null;
                }
                return new DecimalFormat(format).parse(string).doubleValue();
            } catch (ParseException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    };

    @FXML
    private MenuBar systemMenuBar;

    @FXML
    private Menu projectsMenu;

    @FXML
    private MenuItem newProject;
    
    @FXML
    private Label versionLabel;

    private static ControlUi instance;

    private final Control control;

    private Stage settings;

    private Collection<ProjectUi> projectUis;
    private IntegerOffsetStringConverter midiNoteNumberStringConverter;
    private IntegerOffsetStringConverter midiChannelStringConverter;

    private DynamicNoteNameStringConverter midiNoteNameStringConverter;

    private final ObservableList<MidiDevice> midiDevicesBacking;

    private final ObservableList<MidiDevice> midiDevices;

    private final ObservableList<MidiDevice> openMidiDevicesBacking;

    private final ObservableList<MidiDevice> openMidiDevices;

    public static ControlUi createInstance(Control control) {
        ControlUi result = new ControlUi(control);
        control.getMidiDeviceMgr().addListener(instance);
        for (MidiDevice midiDevice : control.getMidiDeviceMgr().getMidiDevices()) {
            if (prefs(midiDevice).getBoolean(OPEN, false)) {
                control.getMidiDeviceMgr().open(midiDevice);
            }
        }

        return result;
    }

    private ControlUi(Control control) {
        midiDevicesBacking = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        midiDevices = FXCollections.unmodifiableObservableList(midiDevicesBacking);
        openMidiDevicesBacking = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        openMidiDevices = FXCollections.unmodifiableObservableList(openMidiDevicesBacking);

        loadFxml(App.MESSAGES);

        projectUis = new CopyOnWriteArrayList<>();

        if (instance != null) {
            throw new IllegalStateException("cannot have two ControlControllers...");
        }
        instance = this;

//        final String os = System.getProperty("os.name");
//        if (os != null && os.startsWith("Mac")) {
//            systemMenuBar.useSystemMenuBarProperty().set(true);
//        }
        this.control = control;

        midiNoteNumberStringConverter = new IntegerOffsetStringConverter();

        midiNoteNumberStringConverter.setOffset(getMidiNoteZeroBased() ? 0 : 1);

        midiNoteNameStringConverter = new DynamicNoteNameStringConverter();

        midiNoteNameStringConverter.setNoteNamingConvention(getNoteNamingConvention());

        midiChannelStringConverter = new IntegerOffsetStringConverter();

        midiChannelStringConverter.setOffset(getMidiChannelZeroBased() ? 0 : 1);

        newProject.setAccelerator(
                new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN));
        versionLabel.setText(Props.getInstance().getVersion() + " PRE-RELEASE");
        Platform.runLater(
                () -> {
                    try {
                        for (String projectPath : openProjectsPrefs.keys()) {
                            try {
                                openProject(Paths.get(projectPath));
                            } catch (IOException e) {
                                LOGGER.error("cannot load project", e);
                            }
                        }
                    } catch (BackingStoreException e) {
                        LOGGER.error("cannot load prefs", e);
                    }
                }
        );
    }

    public static Preferences prefs(MidiDevice midiDevice) {
        Preferences result = systemMidiPrefs.node(MidiDeviceMgr.getPrefsName(midiDevice));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            result.exportNode(stream);
            LOGGER.debug(stream.toString("UTF-8"));
        } catch (BackingStoreException | IOException e) {
            LOGGER.error("this is weird, cannot stream prefs?...", e);
        }
        return result;
    }

    public Control getControl() {
        return control;
    }

    public MidiDeviceMgr getMidiDeviceMgr() {
        return control.getMidiDeviceMgr();
    }

    @FXML
    private void newProject() {
        createStage(getControl().createProject());
    }

    public void openProject(Path path) throws FileNotFoundException, IOException {
        Project project = Project.load(getControl(), path);
        createStage(project);
    }

    @FXML
    private void openProject() throws FileNotFoundException, IOException {
        FileChooser fileChooser = ProjectUi.projectChooser("openProject", getControl().getProjectDirectory());
        fileChooser.getExtensionFilters().addAll(M_FILTER, ALL_FILTER);
        fileChooser.setSelectedExtensionFilter(M_FILTER);
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            openProject(file.toPath());
        }
    }

    @FXML
    public SettingsUi settings() {
        if (settings == null) {
            settings = App.createStage(new SettingsUi(this));
            settings.setResizable(true);
            settings.initOwner(this.getScene().getWindow());
            settings.setTitle(App.MESSAGES.getString("settings"));
            restoreWindowPositionAndSetAutosave(settings, Control.PREFERENCES.node("settings"));
        }
        settings.show();
        settings.toFront();
        return (SettingsUi) settings.getScene().getRoot();
    }

    private static final String PREF_MIDI_NOTE_ZERO_BASED = "midiNoteZeroBased";

    public boolean getMidiNoteZeroBased() {
        return Control.PREFERENCES.getBoolean(PREF_MIDI_NOTE_ZERO_BASED, true);
    }

    public void setMidiNoteZeroBased(boolean zeroBased) {
        midiNoteNumberStringConverter.setOffset(zeroBased ? 0 : 1);
        Control.PREFERENCES.putBoolean(PREF_MIDI_NOTE_ZERO_BASED, zeroBased);
    }

    public IntegerOffsetStringConverter getMidiNoteNumberStringConverter() {
        return midiNoteNumberStringConverter;
    }

    private static final String PREF_MIDI_CHANNEL_ZERO_BASED = "midiChannelZeroBased";

    public boolean getMidiChannelZeroBased() {
        return Control.PREFERENCES.getBoolean(PREF_MIDI_CHANNEL_ZERO_BASED, false);
    }

    public void setMidiChannelZeroBased(boolean zeroBased) {
        midiChannelStringConverter.setOffset(zeroBased ? 0 : 1);
        Control.PREFERENCES.putBoolean(PREF_MIDI_CHANNEL_ZERO_BASED, zeroBased);
    }

    public IntegerOffsetStringConverter getMidiChannelStringConverter() {
        return midiChannelStringConverter;
    }

    private static final String PREF_MIDI_NOTE_NAMING_CONVENTION = "midiNoteNamingConvention";

    public NoteNamingConvention getNoteNamingConvention() {
        try {
            return NoteNamingConvention.valueOf(Control.PREFERENCES.get(PREF_MIDI_NOTE_NAMING_CONVENTION, NoteNamingConvention.ENGLISH_SHORT.name()));
        } catch (RuntimeException ignore) {
            return NoteNamingConvention.ENGLISH_SHORT;
        }
    }

    public void setNoteNamingConvention(NoteNamingConvention noteNamingConvention) {
        Control.PREFERENCES.put(PREF_MIDI_NOTE_NAMING_CONVENTION, noteNamingConvention.name());
        midiNoteNameStringConverter.setNoteNamingConvention(noteNamingConvention);
    }

    public DynamicNoteNameStringConverter getMidiNoteNameStringConverter() {
        return midiNoteNameStringConverter;
    }

    private void createStage(Project project) {
        MenuItem menuItem = new MenuItem();
        ProjectUi projectUi = new ProjectUi(this, project, menuItem);
        projectUis.add(projectUi);
        Stage projectStage = App.createStage(projectUi);
        projectStage.titleProperty().bind(projectUi.nameProperty());

        projectUi.restoreWindowPosition();
        projectStage.show();
        projectUi.createMidiInOutUisFromProjectMidiInOuts();

        menuItem.setOnAction(action -> {
            projectStage.show();
            projectStage.toFront();
        });
        projectsMenu.getItems().add(menuItem);

        projectStage.setOnCloseRequest(onCloseRequest(projectUi));
    }

    private EventHandler<WindowEvent> onCloseRequest(ProjectUi projectUi) {
        return event -> {
            projectUi.closeWindow();
            event.consume();
        };
    }

    public void remove(ProjectUi projectUi) {
        projectUis.remove(projectUi);
    }

    public void restoreWindowPositionAndSetAutosave() {
        restoreWindowPositionAndSetAutosave((Stage) getScene().getWindow(), Control.PREFERENCES);
    }

    public static void restoreWindowPositionAndSetAutosave(Stage stage, Preferences preferences) {
        double x = preferences.getDouble(App.PREF_X, Double.NaN);
        if (x != Double.NaN) {
            stage.setX(x);
            LOGGER.info("init X to <{}>", x);
        }
        double y = preferences.getDouble(App.PREF_Y, Double.NaN);
        if (y != Double.NaN) {
            stage.setY(y);
            LOGGER.info("init Y to <{}>", y);
        }

        stage.showingProperty().addListener(new ChangeListener<Boolean>() {
            // weak reference not needed since there is always exactly one Control(Ui)
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    preferences.putDouble(App.PREF_X, stage.getX());
                    preferences.putDouble(App.PREF_Y, stage.getY());
                    LOGGER.info("moved X,Y to <{}> <{}>", stage.getX(), stage.getY());
                }
            }
        });
    }

    public boolean canCloseWindow() {
        try {
            openProjectsPrefs.clear();
        } catch (BackingStoreException ex) {
            LOGGER.error("huh?", ex);
        }
        for (ProjectUi projectUi : projectUis) {
            openProjectsPrefs.put(projectUi.getPath().toString(), "");
            projectUi.closeWindow();
        }
        return projectUis.isEmpty();
    }

    @FXML
    public boolean closeWindow() {
        boolean result = false;
        if (canCloseWindow()) {
            control.getMidiDeviceMgr().close();
            ((Stage) getScene().getWindow()).close();
            control.getMidiDeviceMgr().removeListener(this);

            // instance = null; no restart, so don't set to null
            result = true;
        }
        return result;
    }

    @Override
    public void midiDeviceAdded(MidiDevice added) {
        Platform.runLater(() -> {
            midiDevicesBacking.add(added);
            midiDevicesBacking.sort(COMPARE_BY_DISPLAY_NAME);
        });
    }

    @Override
    public void midiDeviceRemoved(MidiDevice added) {
        Platform.runLater(() -> midiDevicesBacking.remove(added));
    }

    @Override
    public void midiDeviceOpened(MidiDevice added) {
        Platform.runLater(() -> {
            openMidiDevicesBacking.add(added);
            openMidiDevicesBacking.sort(COMPARE_BY_DISPLAY_NAME);
        });
    }

    @Override
    public void midiDeviceClosed(MidiDevice added) {
        Platform.runLater(() -> {
            openMidiDevicesBacking.remove(added);
        });
    }

    public ObservableList<MidiDevice> getMidiDevices() {
        return midiDevices;
    }

    public ObservableList<MidiDevice> getOpenMidiDevices() {
        return openMidiDevices;
    }

}
