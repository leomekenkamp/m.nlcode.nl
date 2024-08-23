package nl.nlcode.m.cli;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 *
 * @author jq59bu
 */
public class MidiInOutCliRegistry {

    private static final ConcurrentHashMap<String, Function<ControlCli, MidiInOutCli>> NAME_TO_CREATOR = new ConcurrentHashMap<>();
    
    public static void register(String midiInOutType, Function<ControlCli, MidiInOutCli> createInstance) {
        NAME_TO_CREATOR.put(midiInOutType, createInstance);
    }
    
    public static MidiInOutCli create(String midiInOutType, ControlCli controlCli) {
        MidiInOutCli result = NAME_TO_CREATOR.get(midiInOutType).apply(controlCli);
        result.getMidiInOut().openWith(controlCli.getCurrentProject());
        return result;
    }
    
    public static Set<String> midiInOutTypes() {
        return new TreeSet<String>(NAME_TO_CREATOR.keySet());
    }
}
