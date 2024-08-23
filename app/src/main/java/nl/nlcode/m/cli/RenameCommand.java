package nl.nlcode.m.cli;

import java.lang.reflect.InvocationTargetException;
import nl.nlcode.m.engine.MidiInOut;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 *
 * @author jq59bu
 */
@Command(name = "rename", description = "give a MidiInOut instance a different name")
public class RenameCommand extends ChildCommand<BaseCommand> implements Runnable {

    @Parameters(paramLabel = "<current name>", description = "current name of the MidiInOut instance", index = "0")
    private MidiInOut midiInOut;

    @Parameters(paramLabel = "<new name>", description = "new name for the MidiInOut instance", index = "1")
    private String newName;

    @Override
    public void run() {
        midiInOut.setName(newName);
    }

}
