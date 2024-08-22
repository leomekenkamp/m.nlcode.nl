package nl.nlcode.m.cli;

import java.lang.reflect.InvocationTargetException;
import nl.nlcode.m.engine.MidiInOut;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 *
 * @author jq59bu
 */
@Command(name = "new", description = "create new MidiInOut instance")
public class NewCommand extends ChildCommand<BaseCommand> implements Runnable {

    @Parameters(paramLabel = "<MidiInOut type>", description = "type of instance to create", index = "0")
    private String midiInOutType;

    @Parameters(paramLabel = "<name>", description = "name for the new instance", arity = "0..1", index = "1")
    private String name;

    @Override
    public void run() {
        MidiInOutCli midiInOutCli = MidiInOutCliRegistry.create(midiInOutType, getControlCli());
        if (name != null) {
            midiInOutCli.getMidiInOut().setName(name);
        }
    }

}
