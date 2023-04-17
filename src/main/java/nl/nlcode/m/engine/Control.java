package nl.nlcode.m.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.sound.midi.MidiDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public final class Control {

    static {
        AllowedClassesObjectInputStream.allow("[Ljava.util.concurrent.ConcurrentHashMap$Segment;");
        AllowedClassesObjectInputStream.allow("[Ljava.util.concurrent.atomic.AtomicBoolean;");
        AllowedClassesObjectInputStream.allow("[[Ljava.util.concurrent.atomic.AtomicBoolean;");
        AllowedClassesObjectInputStream.allow("java.lang.Boolean");
        AllowedClassesObjectInputStream.allow("java.lang.Double");
        AllowedClassesObjectInputStream.allow("java.lang.Enum");
        AllowedClassesObjectInputStream.allow("java.lang.Number");
        AllowedClassesObjectInputStream.allow("java.util.ArrayList");
        AllowedClassesObjectInputStream.allow("java.util.Collections$SynchronizedCollection");
        AllowedClassesObjectInputStream.allow("java.util.Collections$SynchronizedList");
        AllowedClassesObjectInputStream.allow("java.util.HashMap");
        AllowedClassesObjectInputStream.allow("java.util.HashSet");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.ConcurrentHashMap");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.ConcurrentHashMap$CollectionView");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.ConcurrentHashMap$KeySetView");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.ConcurrentHashMap$Segment");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.atomic.AtomicBoolean");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.locks.AbstractOwnableSynchronizer");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.locks.AbstractQueuedSynchronizer");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.locks.ReentrantLock");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.locks.ReentrantLock$NonfairSync");
        AllowedClassesObjectInputStream.allow("java.util.concurrent.locks.ReentrantLock$Sync");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.KeyboardKeyboard");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiChannelMatrix");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiClock");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiDelay");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiDeviceLink");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiGate");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiInOut");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiLayerAndSplit");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiLayerAndSplit$Layer");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiLights");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiMessageDump");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.MidiSequencer");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.Project");
        AllowedClassesObjectInputStream.allow("nl.nlcode.m.engine.ShowTicks");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Control.class);

    private static final String FILE_EXTENTION = ".m";

    public static final String FILE_EXTENTION_FILTER = "*.m";

    private static final String DEFAULT_FILE_NAME = "noname";

    private static final String DEFAULT_DIR = System.getProperty("user.home");

    private static Control instance;

    private File projectDirectory = new File(DEFAULT_DIR);

    private Set<Project> projects = ConcurrentHashMap.newKeySet();

//    public static final Comparator<? super MidiDevice> COMPARE_BY_DISPLAY_NAME = new Comparator<>() {
//        @Override
//        public int compare(MidiDevice o1, MidiDevice o2) {
//            return getDisplayName(o1).compareTo(getDisplayName(o2));
//        }
//    };
    private final ObservableList<MidiDevice> midiDevicesBacking;

    private final ObservableList<MidiDevice> midiDevices;

    private final ObservableList<MidiDevice> openMidiDevicesBacking;

    private final ObservableList<MidiDevice> openMidiDevices;

    // only used to write to, so if we really want to use this, then refactor
    public Lookup<MidiInOut> lookup = Lookup.create();

    private Control() {
        midiDevicesBacking = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        midiDevices = FXCollections.unmodifiableObservableList(midiDevicesBacking);
        openMidiDevicesBacking = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        openMidiDevices = FXCollections.unmodifiableObservableList(openMidiDevicesBacking);
    }

    public File getProjectDirectory() {
        return projectDirectory;
    }

    public static Control getInstance() {
        if (instance == null) {
            instance = new Control();
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
    }

    void unregister(Project project) {
        projects.remove(project);
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
        // note that we only know this path is unused at this time; might be created by another process
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
}
