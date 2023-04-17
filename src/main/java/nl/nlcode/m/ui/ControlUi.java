package nl.nlcode.m.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
import javax.sound.midi.MidiDevice;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.Control;
import nl.nlcode.m.engine.MidiDeviceMgr;
import nl.nlcode.m.engine.Project;
import static nl.nlcode.m.ui.ProjectUi.M_FILTER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main window.
 *
 * @author leo
 */
public class ControlUi extends BorderPane implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlUi.class);

    public static final FileChooser.ExtensionFilter ALL_FILTER
            = new FileChooser.ExtensionFilter(App.MESSAGES.getString("allFiles"), "*.*");

    public static final FileChooser.ExtensionFilter CSS_FILTER
            = new FileChooser.ExtensionFilter(App.MESSAGES.getString("cssFiles"), "*.css");

    public static final String OPEN = "open";

    public static final int TABLE_VIEW_PREF_HEIGHT = 150;

    @FXML
    private MenuBar systemMenuBar;

    @FXML
    private Menu projectsMenu;

    @FXML
    private MenuItem newProject;

    private static ControlUi instance;

    private final Control control;

    private final MidiDeviceMgr midiDeviceMgr;

    public static final Preferences PREFERENCES = Preferences.userNodeForPackage(Control.class);

    private static final Preferences systemMidiPrefs = PREFERENCES.node("systemMidi");
    
    private static final Preferences openProjectsPrefs = PREFERENCES.node("openProjects");

    private Stage settings;

    private Collection<ProjectUi> projectUis;

    public ControlUi() {
        control = Control.getInstance();
        midiDeviceMgr = MidiDeviceMgr.getInstance();
    }
    
    public ControlUi(Control control, MidiDeviceMgr midiDeviceMgr) {
        loadFxml(App.MESSAGES);

        projectUis = new CopyOnWriteArrayList<>();

        if (instance != null) {
            throw new IllegalStateException("cannot have two ControlControllers...");
        }
        instance = this;
//        final String os = System.getProperty("os.name");
//// To use this below I need to find out how to get rid of the 'java' menu item...
//        if (os != null && os.startsWith("Mac")) {
//            systemMenuBar.useSystemMenuBarProperty().set(true);
//        }

        this.control = control;
        this.midiDeviceMgr = midiDeviceMgr;
        for (MidiDevice midiDevice : midiDeviceMgr.getMidiDevices()) {
            if (prefs(midiDevice).getBoolean(OPEN, false)) {
                midiDeviceMgr.open(midiDevice);
            }
        }
        newProject.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN));
        Platform.runLater(() -> {
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
        });
    }

    public static Preferences prefs(MidiDevice midiDevice) {
        Preferences result = systemMidiPrefs.node(MidiDeviceMgr.getDisplayName(midiDevice));
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
        return midiDeviceMgr;
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
    private void settings() {
        if (settings == null) {
            settings = App.createStage(new SettingsUi(getControl(), MidiDeviceMgr.getInstance()));
            settings.initOwner(this.getScene().getWindow());
            settings.setTitle(App.MESSAGES.getString("settings"));
            restoreWindowPositionAndSetAutosave(settings, PREFERENCES.node("settings"));
        }
        settings.show();
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
        restoreWindowPositionAndSetAutosave((Stage) getScene().getWindow(), PREFERENCES);
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
            midiDeviceMgr.close();
            ((Stage) getScene().getWindow()).close();
            // instance = null; no restart, so don't set to null
            result = true;
        }
        return result;
    }

}
