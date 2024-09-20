package nl.nlcode.m.cli;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.Project;
import picocli.CommandLine;

/**
 * The 'invisible' command that precedes any command on the cli.
 *
 * @author jq59bu
 */
@CommandLine.Command(name = "list", description = "display list of MidiInOut instances")
public class ListCommand extends WithProjectCommand<BaseCommand> implements Runnable {

    @Override
    public void run() {
        withSelectedProjects(project -> {
            getControlCli().printList("list", 
                List.of(project.getPath()), 
                (Iterable<MidiInOut<?>>) project.getMidiInOutLookup(), 
                (midiInOut) -> List.of(midiInOut.getHumanTypeName(), midiInOut.getName())
            );
        });
    }
}
