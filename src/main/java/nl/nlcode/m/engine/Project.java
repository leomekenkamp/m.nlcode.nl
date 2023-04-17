package nl.nlcode.m.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author leo
 */
public final class Project implements Serializable {

    private static final long serialVersionUID = 0L;

    private transient Control control;

    private transient Path path;

    private transient Lookup<MidiInOut> midiInOutLookup;

    private transient ObservableList<MidiInOut> midiInOutList;
    
    private transient ExecutorService midiInOutExecutorService;

    private Map<Serializable, Serializable> info = new HashMap<>();
    
    
    public static Project load(Control control, Path path) throws FileNotFoundException, IOException {
//main();

        try (ObjectInputStream in = new AllowedClassesObjectInputStream(new FileInputStream(path.toFile()))) {
            Project result = (Project) in.readObject();
            result.init(control, path);
            ArrayList<MidiInOut> justRead = (ArrayList<MidiInOut>) in.readObject();
            for (MidiInOut midiInOut : justRead) {
                midiInOut.init(result.midiInOutLookup, ForkJoinPool.commonPool());
            }
            return result;
        } catch (ClassNotFoundException e) { // our files should never contain unknown classes, so fail hard here
            throw new IllegalStateException(e);
        }
    }

    public static Project create(Control control, Path path) {
        if (Files.exists(path)) {
            throw new IllegalArgumentException("path already exists: <" + path + ">");
        }
        Project result = new Project();
        result.init(control, path);
        // we explicitly do NOT call 'save()' here, because the responsibility to save belongs to the user
        return result;
    }

    private Project() {
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public void close() {
        control.unregister(this);
    }

    private void init(Control control, Path path) {
        midiInOutExecutorService = ForkJoinPool.commonPool();
        this.control = control;
        this.path = path;
        midiInOutList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        midiInOutLookup = Lookup.createWithSynchronizedBacking(midiInOutList);
        control.register(this);
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public Control getControl() {
        return control;
    }

    public void saveAs(Path path) throws IOException {
        path = path.normalize();
        Path oldPath = path;
        setPath(path);
        try {
            save();
        } catch (IOException e) {
            this.setPath(oldPath);
            throw e;
        }
    }

    public void save() throws IOException {
        // TODO save to backup file
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getPath().toFile()))) {
            out.writeObject(this);
            out.writeObject(new ArrayList(midiInOutList));
        }
    }

    public Lookup<MidiInOut> getMidiInOutLookup() {
        return midiInOutLookup;
    }

    public ExecutorService getMidiInOutExecutorService() {
        return midiInOutExecutorService;
    }
    
    public ObservableList<MidiInOut> getMidiInOutList() {
        return midiInOutList;
    }

    public Map<Serializable, Serializable> getInfo() {
        return info;
    }
}
