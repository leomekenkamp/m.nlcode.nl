package nl.nlcode.m.cli;

import java.io.PrintWriter;
import nl.nlcode.m.engine.MidiInOut;
import picocli.CommandLine;

/**
 * The 'invisible' command that precedes any command on the cli.
 *
 * @author jq59bu
 */
@CommandLine.Command(name = "list", description="display list of MidiInOut instances")
public class ListCommand extends ChildCommand<BaseCommand> implements Runnable {
    
    @Override
    public void run() {
        PrintWriter stdout = getControlCli().stdout();
        StringBuilder items = new StringBuilder();
        for (MidiInOut midiInOut : getControlCli().getDefaultProject().getMidiInOutLookup()) {
            items.append(getControlCli().commandMessage("list.item", midiInOut.getClass().getSimpleName(), midiInOut.getName()));
        }
        stdout.print(getControlCli().commandMessage("list", items.toString(), getControlCli().getDefaultProject().getPath()));
    }
}
