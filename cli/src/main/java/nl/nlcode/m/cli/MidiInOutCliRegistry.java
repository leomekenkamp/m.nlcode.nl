package nl.nlcode.m.cli;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import nl.nlcode.m.engine.Project;

/**
 *
 * @author jq59bu
 */
public class MidiInOutCliRegistry {

    private static final ConcurrentHashMap<String, Function<ControlCli, MidiInOutCli>> NAME_TO_CREATOR = new ConcurrentHashMap<>();
    
    public static void register(String midiInOutType, Function<ControlCli, MidiInOutCli> createInstance) {
        NAME_TO_CREATOR.put(midiInOutType, createInstance);
    }
    
    public static MidiInOutCli create(String midiInOutType, ControlCli controlCli, Project project) {
        MidiInOutCli result;
        Function<ControlCli, MidiInOutCli> creator = NAME_TO_CREATOR.get(midiInOutType);
        if (creator == null) {
            result = null;
        } else {
            result = creator.apply(controlCli);
            result.getMidiInOut().openWith(project);
        }
        return result;
    }
    
    public static Set<String> midiInOutTypes() {
        return new TreeSet<String>(NAME_TO_CREATOR.keySet());
    }
}
