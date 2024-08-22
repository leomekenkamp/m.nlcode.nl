package nl.nlcode.m.cli;

import java.util.Set;
import nl.nlcode.m.engine.MidiInOut;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 *
 * @author jq59bu
 */
@Command(name = "connect", description = "No or one parameter: show output links.%nTwo parameters: link a sending MidiInOut instance to a receiving one.")
public class ConnectCommand extends ChildCommand<BaseCommand> implements Runnable {

    @Option(names = "--reverse", description = "Reverses this command to 'receiving' and exchange sender and receiver.")
    private boolean reverse;

    @Parameters(paramLabel = "<sender>", description = "name of sending MidiInOut instance", arity = "0..1", index = "0")
    private MidiInOut first;

    @Parameters(paramLabel = "<receiver>", description = "name for the receiving MidiInOut instance", arity = "0..1", index = "1")
    private MidiInOut second;
    
    @Override
    public void run() {
        if (first == null) {
            for (MidiInOut s : getControlCli().getDefaultProject().getMidiInOutLookup()) {
                final Set<MidiInOut> others = reverse ? s.receivingFrom() : s.sendingTo();
                if (!others.isEmpty()) {
                    getControlCli().stdout().println(s.getName());
                }
                for (MidiInOut r : others) {
                    getControlCli().stdout().print(reverse ? "   ← " : "   → ");
                    getControlCli().stdout().println(r.getName());
                }
            }
        } else if (second == null) {
            final Set<MidiInOut> others = reverse? first.receivingFrom() : first.sendingTo();
            for (MidiInOut r : others) {
                getControlCli().stdout().print(reverse ? "   ← " : "   → ");
                getControlCli().stdout().println(r.getName());
            }
        } else {
            if (reverse) {
                second.startSendingTo(first);
            } else {
                first.startSendingTo(second);
            }
        }            
    }

}
