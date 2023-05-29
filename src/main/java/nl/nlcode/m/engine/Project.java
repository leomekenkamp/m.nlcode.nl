package nl.nlcode.m.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
            List<Marshalled> midiInOutList)
            implements Marshalled<Project> {

        @Override
        public void unmarshalInternal(Marshalled.Context context, Project target) {
            target.getInfo().putAll(info);
            for (Marshalled<MidiInOut> marshalled : midiInOutList()) {
                MidiInOut midiInOut = marshalled.unmarshal(context);
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
        return new SaveData0(id, info, context.toSaveDataList(midiInOutList));
    }

    private static final long serialVersionUID = 0L;

    private transient Control control;

    private transient Path path;

    private transient Lookup<MidiInOut> midiInOutLookup;

    private transient ObservableList<MidiInOut> midiInOutList;

    private transient ExecutorService midiInOutExecutorService;

    private Map<Serializable, Serializable> info = new HashMap<>();

    public static Project load(Control control, Path path) throws FileNotFoundException, IOException {
        ObjectMapper objectMapper = createObjectMapper();
        objectMapper.readValue(new FileInputStream(path.toFile()), Project.SaveData0.class);

        SaveData0 saveData = objectMapper.readValue(new FileInputStream(path.toFile()), Project.SaveData0.class);
        Marshalled.Context context = new Marshalled.Context();
        Project result = saveData.unmarshal(context);
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
        // we explicitly do NOT call 'save()' here, because the responsibility to save belongs to the user
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

    private static ObjectMapper createObjectMapper() {
        ObjectMapper result = new ObjectMapper();
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("nl.nlcode")
                .allowIfSubType("java.util.ArrayList")
                .allowIfSubType("java.util.HashMap")
                .build();
        result.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        return result;
    }

    public void save() throws IOException { //TODO backup file
        ObjectMapper objectMapper = createObjectMapper();

        Marshallable.Context marshallableContext = new Marshallable.Context();
        Marshalled marshalled = marshallableContext.marshal(this);

        objectMapper.writeValue(new FileOutputStream(getPath().toFile()), marshalled);
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
}
