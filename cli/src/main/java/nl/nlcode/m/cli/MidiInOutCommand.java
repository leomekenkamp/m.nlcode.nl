package nl.nlcode.m.cli;

import java.util.Set;
import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.Project;
import nl.nlcode.m.linkui.IntUpdater;
import nl.nlcode.m.linkui.Updater;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 *
 * @author jq59bu
 */
@Command(description = "manipulates the midiInOut instance with this name")
public class MidiInOutCommand<M extends MidiInOut> extends ChildCommand<BaseCommand> implements Runnable {

    @Spec
    private CommandSpec commandSpec;

    private MidiInOutCommand() {
    }

    private MidiInOut lookup(Project project, String name) {
        MidiInOut result = project.getMidiInOutLookup().get(name);
        if (result == null) {
            getControlCli().commandOutput("MidiInOut.name.none", name, project.getPath());
        }
        return result;
    }

    /**
     * @return {@code MidiInOut#getName}
     */
    private String getName() {
        return commandSpec.name();
    }

    public M getMidiInOut() {
        return (M) getControlCli().getCurrentProject().getMidiInOutLookup().get(getName());
    }

    @Command(name = "connect", description = "Either shows output connections to other midiInOut instances (no parameter), or connects a sending midiInOut instances to a receiving one.")
    public void connect(
            @Option(names = {"--reverse", "-r"}, description = "Show or set input instead of output connections.") boolean reverse,
            @Parameters(paramLabel = "<receiver>", description = "name for the receiving MidiInOut instance", arity = "0..1") String secondName) {
        MidiInOut first = getMidiInOut();
        if (secondName == null) {
            final Set<MidiInOut> others = reverse ? first.receivingFrom() : first.sendingTo();
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

    @Command(name = "disconnect", description = "Disconnects a sending midiInOut instance from to a receiving ones.")
    public void disconnect(
            @Option(names = {"--reverse", "-r"}, description = "Remove input instead of output connections.") boolean reverse,
            @Parameters(paramLabel = "<receiver>", description = "name for the receiving MidiInOut instance", arity = "1") String secondName) {
        MidiInOut first = getMidiInOut();
        MidiInOut second = lookup(getControlCli().getCurrentProject(), secondName);
        if (reverse) {
            second.stopSendingTo(first);
        } else {
            first.stopSendingTo(second);
        }
    }

    @Command(name = "rename", description = "give the MidiInOut instance a different name")
    public void rename(
            @Parameters(paramLabel = "<new name>", description = "new name for the MidiInOut instance") String newName) {
        getMidiInOut().setName(newName);
    }

    public static CommandSpec createCommandSpec(MidiInOut midiInOut) {
        CommandSpec result = CommandSpec.forAnnotatedObject(new MidiInOutCommand());
        Iterable<Updater> allUpdaters = midiInOut.getAllUpdaters();
        for (Updater updater : allUpdaters) {
            if (updater instanceof IntUpdater intUpdater) {
                CommandSpec updaterSpec = CommandSpec.forAnnotatedObject(new IntUpdaterCommand(intUpdater));
                result.addSubcommand(updater.getName(), updaterSpec);
            }
        }
        return result;
    }

    public void run() {
        getControlCli().stdout().println("settings:");
        Iterable<Updater> allUpdaters = getMidiInOut().getAllUpdaters();
        for (Updater updater : allUpdaters) {
            getControlCli().stdout().println(updater.getName() + ": " + updater.getValue());
        }
    }

}
