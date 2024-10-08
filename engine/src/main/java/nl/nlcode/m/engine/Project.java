package nl.nlcode.m.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
                midiInOut.openWith(target);
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

    private static final SaverLoader SAVER_LOADER = new SaverLoader();

    private transient Control control;

    private transient Path path;

    private transient Lookup<MidiInOut<?>> midiInOutLookup;

    private transient List<MidiInOut<?>> midiInOutList;

    private transient ExecutorService midiInOutExecutorService;

    private transient boolean dirty;

    private Map<Serializable, Serializable> info = new HashMap<>();

    public static Project load(Control control, Path path) throws FileNotFoundException, IOException {
        SaverLoader saverLoader = new SaverLoader();
        Project result = SAVER_LOADER.load(path);
        result.init(control, path);
        result.fullyLoaded();
        result.resetDirty();
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
//        midiInOutExecutorService = ForkJoinPool.commonPool();
        midiInOutExecutorService = new ThreadPoolExecutor(10, 10, 1, TimeUnit.DAYS, new LinkedBlockingQueue());
        midiInOutList = new ArrayList<>();
        midiInOutLookup = Lookup.createWithSynchronizedBacking(midiInOutList);
    }

    public boolean close(boolean force) {
        if (!dirty || force) {
            control.unregister(this);
            return true;
        } else {
            return false;
        }
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

    public Path getProjectExtPath(String name) {
         Path parent = getPath().getParent();
         String projectName = getPath().getName(getPath().getNameCount() - 1).toString();
         String baseName;
         if (projectName.endsWith(Control.FILE_EXTENTION)) {
             baseName = projectName.substring(0, projectName.length() - Control.FILE_EXTENTION.length());
         } else {
             baseName = projectName;
         }
         return Path.of(parent.toString(), baseName + name);
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
        resetDirty();
    }

    public void saveIfDirty() throws IOException {
        if (dirty) {
            save();
        }
    }
    
    public Lookup<MidiInOut<?>> getMidiInOutLookup() {
        return midiInOutLookup;
    }

    public ExecutorService getMidiInOutExecutorService() {
        return midiInOutExecutorService;
    }

    public List<MidiInOut<?>> getMidiInOutList() {
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

    private void resetDirty() {
        dirty = false;
    }

}
