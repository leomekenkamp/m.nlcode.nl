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
@Command(name = "disconnect", description = "One parameter: disconnect all receivers from sender.%nTwo parameters: stop sending from sender to receiver.")
public class DisconnectCommand extends ChildCommand<BaseCommand> implements Runnable {

    @Option(names = "--reverse", description = "Reverses this command to 'receiving' and exchange sender and receiver.")
    private boolean reverse;

    @Parameters(paramLabel = "<sender>", description = "name of sending MidiInOut instance", index = "0")
    private MidiInOut first;

    @Parameters(paramLabel = "<receiver>", description = "name of the receiving MidiInOut instance", arity = "0..1", index = "1")
    private MidiInOut second;

    @Override
    public void run() {
        if (second == null) {
            if (reverse) {
                final Set<MidiInOut> others = first.receivingFrom();
                for (MidiInOut o : others) {
                    o.stopSendingTo(first);
                }
            } else {
                final Set<MidiInOut> others = first.sendingTo();
                for (MidiInOut o : others) {
                    first.stopSendingTo(o);
                }
            }
        } else {
            if (reverse) {
                second.stopSendingTo(first);
            } else {
                first.stopSendingTo(second);
            }
        }
    }

}
