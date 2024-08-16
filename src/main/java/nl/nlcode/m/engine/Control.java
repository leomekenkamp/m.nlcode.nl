package nl.nlcode.m.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.sound.midi.MidiDevice;
import nl.nlcode.m.Prefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public final class Control {

    public static interface Ui {

        void added(Project project);
        
        void removed(Project project);
    }

    private Set<Ui> uis = Collections.newSetFromMap(new WeakHashMap<>());

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String FILE_EXTENTION = ".m";

    public static final String FILE_EXTENTION_FILTER = "*.m";

    private static final String DEFAULT_FILE_NAME = "noname";

    private static final String DEFAULT_DIR = System.getProperty("user.home");

    private static Control instance;
    public static final String OPEN = "open";

    private final Preferences preferences;

    private final Preferences safeFilePreferences;

    private final Preferences systemMidiPreferences;

    public Preferences getPreferences(MidiDevice midiDevice) {
        Preferences result = systemMidiPreferences.node(MidiDeviceMgr.getPrefsName(midiDevice));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            result.exportNode(stream);
            LOGGER.debug(stream.toString("UTF-8"));
        } catch (BackingStoreException | IOException e) {
            LOGGER.error("this is weird, cannot stream prefs?...", e);
        }
        return result;
    }

    private File projectDirectory = new File(DEFAULT_DIR);

    private Set<Project> projects = ConcurrentHashMap.newKeySet();

    private MidiDeviceMgr midiDeviceMgr;

    public Control(boolean test) {
        midiDeviceMgr = MidiDeviceMgr.getInstance();
        preferences = Prefs.forApp().userNodeForPackage(Control.class);
        safeFilePreferences = preferences.node("saveFiles");
        systemMidiPreferences = preferences.node("systemMidi");
        for (MidiDevice midiDevice : getMidiDeviceMgr().getMidiDevices()) {
            if (getPreferences(midiDevice).getBoolean(OPEN, false)) {
                getMidiDeviceMgr().open(midiDevice);
            }
        }
    }

    public void addUi(Ui ui) {
        uis.add(ui);
    }
    
    public void removeUi(Ui ui) {
        uis.remove(ui);
    }
    
    public Preferences getPreferences() {
        return preferences;
    }

    public MidiDeviceMgr getMidiDeviceMgr() {
        return midiDeviceMgr;
    }

    public File getProjectDirectory() {
        return projectDirectory;
    }

    public static Control getInstance() {
        if (instance == null) {
            instance = new Control(true);
        }
        return instance;
    }

    public Project createProject() {
        return Project.create(this, unusedProjectPath());
    }

    public Project loadProject(Path path) throws IOException {
        return Project.load(this, path);
    }

    void register(Project project) {
        projects.add(project);
        updateUi(ui ->  ui.added(project));
    }

    void unregister(Project project) {
        projects.remove(project);
        updateUi(ui ->  ui.removed(project));
    }

    private void updateUi(Consumer<Ui> update) {
        uis.stream().forEach(update);
    }
    
    public Path unusedProjectPath() {
        int index = -1;
        DecimalFormat format = new DecimalFormat("00");
        Path result;
        do {
            if (++index > 99) {
                throw new FunctionalException("seems there are too much '" + DEFAULT_FILE_NAME + FILE_EXTENTION_FILTER + "' files");
            }
            result = Path.of(DEFAULT_DIR, DEFAULT_FILE_NAME + format.format(index) + FILE_EXTENTION);
        } while (Files.exists(result) || unsavedPathInUse(result));
        // note that we only know this path is unused at _this_ time; might be created by _another_ process shortly after
        return result;
    }

    private boolean unsavedPathInUse(Path path) {
        for (Project project : projects) {
            if (project.getPath().equals(path)) {
                return true;
            }
        }
        return false;
    }

    public SaveFileEncoding getSaveFileEncoding() {
        try {
            return SaveFileEncoding.fromDesc(safeFilePreferences.get("encoding", SaveFileEncoding.GZIP.toDesc()));
        } catch (RuntimeException ignore) {
            return SaveFileEncoding.GZIP;
        }
    }

    public void setSaveFileEncoding(SaveFileEncoding encoding) {
        safeFilePreferences.put("encoding", encoding.toDesc());
    }

    public int getProjectCount() {
        return projects.size();
    }
}
