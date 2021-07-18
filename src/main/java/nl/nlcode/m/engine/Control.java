package nl.nlcode.m.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;

/**
 *
 * @author leo
 */
public final class Control {
    
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

//    public static List<String> getCapabilities(MidiDevice device) {
//        List<String> result = new ArrayList<>();
//        if (device.getMaxReceivers() != 0) {
//            result.add("receiver");
//        }
//        if (device.getMaxTransmitters() != 0) {
//            result.add(("sender"));
//        }
//        return result;
//    }

//    public static String getCapabilitiesDecription(MidiDevice device) {
//        List<String> caps = getCapabilities(device);
//        if (caps.isEmpty()) {
//            return "";
//        } else {
//            return "(" + String.join(", ", caps) + ")";
//        }
//    }
//
//    public static String getDisplayName(MidiDevice device) {
//        if (device == null) {
//            return "<empty>";
//        } else {
//            return getDisplayName(device.getDeviceInfo()) + getCapabilitiesDecription(device);
//        }
//    }
//
//    public static String getDisplayName(MidiDevice.Info info) {
//        StringBuilder result = new StringBuilder(info.getName());
//        if (info.getDescription() != null) {
//            result.append(", ").append(info.getDescription());
//        }
//        if (info.getVendor() != null) {
//            result.append(" - ").append(info.getVendor());
//        }
//        // We skip the version, because this display name is used to identify devices over different
//        // executions of the program. So a newer version does probably mean that the same settings should
//        // be used.
//        return result.toString();
//    }

//    public ObservableList<MidiDevice> getMidiDevices() {
//        return midiDevices;
//    }
//
//    public ObservableList<MidiDevice> getOpenMidiDevices() {
//        return openMidiDevices;
//    }

//    public void close() {
//        // We explicitly close ONLY the java midi devices. We want our devices to be reopened automatically
//        // the next time the application starts.
//        for (MidiDevice midiDevice : midiDevicesBacking) {
//            if (midiDevice.isOpen()) {
//                LOGGER.debug("closing {}", midiDevice);
//                midiDevice.close();
//            }
//        };
//    }

//    public void open(MidiDevice device) {
//        try {
//            if (device.isOpen()) {
//                throw new IllegalArgumentException("already open: " + device);
//            } else {
//                LOGGER.debug("opening {}", device);
//                device.open();
//                synchronized(openMidiDevicesBacking) {
//                    openMidiDevicesBacking.add(device);
//                    LOGGER.debug("now open: {}", openMidiDevicesBacking);
//                    Collections.sort(openMidiDevicesBacking, COMPARE_BY_DISPLAY_NAME);
//                }
//            }
//        } catch (MidiUnavailableException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//
//    public void close(MidiDevice device) {
//        if (device.isOpen()) {
//            LOGGER.debug("closing {}", device);
//            device.close();
//            synchronized(openMidiDevicesBacking) {
//                openMidiDevicesBacking.remove(device);
//            }
//        } else {
//                throw new IllegalArgumentException("already closed: " + device);
//        }
//    }
}
