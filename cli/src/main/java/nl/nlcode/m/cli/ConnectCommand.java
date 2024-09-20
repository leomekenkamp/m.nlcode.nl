package nl.nlcode.m.cli;

import java.util.Set;
import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.Project;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 *
 * @author jq59bu
 */
@Command(description = "Either shows output connections to other midiInOut instances (no parameter), or connects a sending midiInOut instances to a receiving one.")
public class ConnectCommand extends ChildCommand<MidiInOutCommand> implements Runnable {

    @Option(names = {"--reverse", "-r"}, description = "Show or set input instead of output connections.")
    private boolean reverse;

    @Parameters(paramLabel = "<receiver>", description = "name for the receiving MidiInOut instance", arity = "0..1")
    private String secondName;

    @Override
    public void run() {
        MidiInOut first = getParent().getMidiInOut();
        if (secondName == null) {
            final Set<MidiInOut> others = reverse ? first.receivingFrom() : getParent().getMidiInOut().sendingTo();
            for (MidiInOut r : others) {
                getControlCli().stdout().println(r.getName());
            }
        } else {
            MidiInOut second = lookup(getControlCli().getCurrentProject(), secondName);
            if (reverse) {
                second.startSendingTo(first);
            } else {
                first.startSendingTo(second);
            }
        }
    }

    private MidiInOut lookup(Project project, String name) {
        MidiInOut result = project.getMidiInOutLookup().get(name);
        if (result == null) {
            getControlCli().printMessage("MidiInOut.name.none", name, project.getPath());
        }
        return result;
    }

}
