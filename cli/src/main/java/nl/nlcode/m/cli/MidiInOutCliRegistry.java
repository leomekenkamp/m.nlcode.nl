package nl.nlcode.m.cli;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import nl.nlcode.m.engine.KeyboardKeyboard;
import nl.nlcode.m.engine.Lights;
import nl.nlcode.m.engine.MidiDeviceLink;
import nl.nlcode.m.engine.Project;

/**
 *
 * @author jq59bu
 */
public class MidiInOutCliRegistry {

    private static final MidiInOutCliRegistry midiInOutCliRegistry;
    
    public static MidiInOutCliRegistry getInstance() {
        return midiInOutCliRegistry;
    }
    
    static {
        midiInOutCliRegistry = new MidiInOutCliRegistry();
        midiInOutCliRegistry.register("lights", (controlCli) -> new LightsCli(new Lights(), controlCli));
        midiInOutCliRegistry.register("midiDeviceLink", (controlCli) -> new MidiDeviceLinkCli(new MidiDeviceLink(), controlCli));
        midiInOutCliRegistry.register("keyboardKeyboard", (controlCli) -> new KeyboardKeyboardCli(new KeyboardKeyboard(), controlCli));
    }
    
    private ConcurrentHashMap<String, Function<ControlCli, MidiInOutCli>> nameToCreator = new ConcurrentHashMap<>();
    
    private MidiInOutCliRegistry() {
    }
    
    private void register(String midiInOutType, Function<ControlCli, MidiInOutCli> createInstance) {
        nameToCreator.put(midiInOutType, createInstance);
    }
    
    public MidiInOutCli create(String midiInOutType, String name, ControlCli controlCli, Project project) {
        MidiInOutCli result;
        Function<ControlCli, MidiInOutCli> creator = nameToCreator.get(midiInOutType);
        if (creator == null) {
            result = null;
        } else {
            result = creator.apply(controlCli);
            result.getMidiInOut().setName(name);
            result.getMidiInOut().openWith(project);
        }
        return result;
    }
    
    public Set<String> midiInOutTypes() {
        return new TreeSet<String>(nameToCreator.keySet());
    }
}
