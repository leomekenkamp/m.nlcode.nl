package nl.nlcode.m.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import nl.nlcode.marshalling.MarshalHelper;
import nl.nlcode.marshalling.Marshallable;
import nl.nlcode.marshalling.Marshalled;

/**
 *
 * @author leo
 */
public final class Project implements Serializable, Marshallable {

    public static record SaveData0(
            int id,
            Map<Serializable, Serializable> info,
            //            List<Marshalled<MidiInOut>> midiInOutList)
            Marshalled<? extends MidiInOut>[] midiInOutList)
            implements Marshalled<Project> {

        @Override
        public void unmarshalInto(Marshalled.Context context, Project target) {
            target.getInfo().putAll(info);
            for (Marshalled<? extends MidiInOut> marshalled : midiInOutList()) {
                MidiInOut midiInOut = MarshalHelper.unmarshal(context, marshalled);
                midiInOut.activate(target);
            }
        }

        @Override
        public Project createMarshallable() {
            return new Project();
        }

    }

    @Override
    public SaveData0 marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                info,
                //                MarshalHelper.toList(context, midiInOutList)
MarshalHelper.marshallToArray(context, midiInOutList)
        );
    }

    private static final long serialVersionUID = 0L;

    private static final SaverLoader SAVER_LOADER = new SaverLoader();
    
    private transient Control control;

    private transient Path path;

    private transient Lookup<MidiInOut> midiInOutLookup;

    private transient ObservableList<MidiInOut> midiInOutList;

    private transient ExecutorService midiInOutExecutorService;
    
    private transient boolean dirty;

    private Map<Serializable, Serializable> info = new HashMap<>();

    public static Project load(Control control, Path path) throws FileNotFoundException, IOException {
        SaverLoader saverLoader = new SaverLoader();
        Project result = SAVER_LOADER.load(path);
        result.init(control, path);
        result.fullyLoaded();
        return result;
    }

    public static Project create(Control control, Path path) {
        if (Files.exists(path)) {
            throw new IllegalArgumentException("path already exists: <" + path + ">");
        }
        Project result = new Project();
        result.init(control, path);
        result.fullyLoaded();
        // Explicitly do NOT call 'save()' here, because the responsibility to save belongs to the user.
        // A 'create' action should not cause file system clutter.
        return result;
    }

    private Project() {
        midiInOutExecutorService = ForkJoinPool.commonPool();
        midiInOutList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        midiInOutLookup = Lookup.createWithSynchronizedBacking(midiInOutList);
    }

    public void close() {
        control.unregister(this);
    }

    private void init(Control control, Path path) {
        this.control = control;
        this.path = path;
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
        SAVER_LOADER.save(this);
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

    private void fullyLoaded() {
        for (MidiInOut midiInOut : midiInOutList) {
            midiInOut.projectFullyLoaded();
        }
    }
    
    public void setDirty() {
        dirty = true;
    }
        
    private void dirtyReset() {
        dirty = false;
    }

}
