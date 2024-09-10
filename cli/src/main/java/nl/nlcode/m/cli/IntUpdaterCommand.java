package nl.nlcode.m.cli;

import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.linkui.IntUpdater;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 *
 * @author jq59bu
 */
@Command
public class IntUpdaterCommand<M extends MidiInOut> extends ChildCommand<MidiInOutCommand> implements Runnable {
    
    @CommandLine.Parameters(paramLabel = "<new_value>", description = "new value for this setting", arity = "0..1")
    private Integer newValue;

    private IntUpdater intUpdater;
    
    public IntUpdaterCommand(IntUpdater intUpdater) {
        this.intUpdater = intUpdater;
    }
    
    protected IntUpdater getIntUpdater() {
        return intUpdater;
    }
    
    public void run() {
        if (newValue == null) {
            getControlCli().stdout().println(intUpdater.getName()+ ": " + intUpdater.get());
        } else {
            intUpdater.set(newValue);
        }
    }
    
}
